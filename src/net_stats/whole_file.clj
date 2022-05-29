(ns net-stats.whole-file
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [net-stats.item-util :as item_util]))

(def separating_line_regex
  (re-pattern
   (str "(?sim)^" item_util/separating_line)))

(defn get_file
  [name]
  (slurp name))

(defn get_chunks
  [text]
  (str/split text separating_line_regex))

(defn remove_empty_chunks
  [chunks]
  (filter item_util/not_blank_text_chunk chunks))

(defn remove_nil_chunks
  [chunks]
  (filter some? chunks))

(defn build_data_chunks
  [chunks interface]
  (->> chunks
       (remove_empty_chunks)
       (map #(item_util/get_data_from_chunk % interface))
       (remove_nil_chunks)))

(defn get_data_for_interface
  [data_chunk interface]
  (let [timestamp (:ts data_chunk)
        rx (get-in data_chunk [:data :rx])
        tx (get-in data_chunk [:data :tx])]
    {:timestamp timestamp
     :if interface
     :rx rx
     :tx tx}))

(defn filter_by_interface
  [data_chunks interface]
  (map #(get_data_for_interface % interface) data_chunks))

(defn compare_dates
  [a b]
  (.compareTo a b))

(defn sort_by_datetime
  [data_chunks]
  (sort-by :timestamp compare_dates data_chunks))

(defn is_valid_chunk
  [chunk]
  (and
    (some? (:timestamp chunk))
    (some? (:if chunk))
    (some? (:rx chunk))
    (some? (:tx chunk))))

(defn remove_invalid_chunks
  [chunks]
  (filter is_valid_chunk chunks))

(defn get_data
  [name interface]
  (let [text (get_file name)
        chunks (get_chunks text)
        data_chunks (build_data_chunks chunks interface)
        specific_chunks (filter_by_interface data_chunks interface)
        valid (remove_invalid_chunks specific_chunks)
        sorted (sort_by_datetime valid)]
    sorted))

(defn print_result
  [chunks]
  (doseq [chunk chunks
         :let [ts (:timestamp chunk)
               if (:if chunk)
               rx (:rx chunk)
               tx (:tx chunk)
               rx_delta (:rx_delta chunk)
               tx_delta (:tx_delta chunk)]]
    (println (.format item_util/formatter ts) if rx tx rx_delta tx_delta)))

(defn calc_one_delta
  [{rxa :rx
    txa :tx
    :as a}
   {rxb :rx
    txb :tx}]
  (let [rx_delta (- rxb rxa)
        tx_delta (- txb txa)]
    (assoc a :rx_delta rx_delta :tx_delta tx_delta)
    (cond
      (neg? rx_delta) (assoc a :rx_delta 0 :tx_delta 0)
      (neg? tx_delta) (assoc a :rx_delta 0 :tx_delta 0)
      :else (assoc a :rx_delta rx_delta :tx_delta tx_delta))))

(defn fill_stub_delta
  [a]
  (assoc a :rx_delta 0 :tx_delta 0))

(defn prepare_one_delta
  [a b]
  (if (some? b)
    (calc_one_delta a b)
    (fill_stub_delta a)))

(defn calc_delta
  [items]
  (let [tail (rest items)]
    (map prepare_one_delta items tail)))

(defn process_whole_file_at_once
  [file interface]
    (let [only_rx_tx (get_data file interface)
        with_delta (calc_delta only_rx_tx)]
      (print_result with_delta)))

