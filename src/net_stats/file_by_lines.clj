(ns net-stats.file-by-lines
  (:require
   [clojure.java.io :as io]
   [clojure.tools.trace :as trc]
   [net-stats.item-util :as item_util]))

(defn separator?
  [line]
  (= line item_util/separating_line))

(defn is_everything_valid?
  [items]
  (every? some? items))

(defn calc_and_print_delta
  [timestamp interface rx1 tx1 rx2 tx2]
  (let [rx_delta (max 0 (- rx2 rx1))
        tx_delta (max 0 (- tx2 tx1))]
    (println (.format item_util/formatter timestamp) interface rx1 tx1 rx_delta tx_delta)))

(defn print_chunk_delta
  [interface prev_data_chunk new_data_chunk]
  (let [timestamp1 (:ts prev_data_chunk)
        rx1 (get-in prev_data_chunk [:data :rx])
        tx1 (get-in prev_data_chunk [:data :tx])
        rx2 (get-in new_data_chunk [:data :rx])
        tx2 (get-in new_data_chunk [:data :tx])]
    (if (is_everything_valid? [timestamp1 rx1 tx1 rx2 tx2])
      (calc_and_print_delta timestamp1 interface rx1 tx1 rx2 tx2)
      nil ;; (.println *err* (str "invalid chunks, prev: " prev_data_chunk ", new: " new_data_chunk))
      )))

(defn process_text_chunk
  [chunk interface prev_data_chunk]
  (let [new_data_chunk (item_util/get_data_from_chunk chunk interface)]
    (print_chunk_delta interface prev_data_chunk new_data_chunk)
    new_data_chunk))

(defn process_item
  [chunk interface prev_data_chunk]
  (if (item_util/not_blank_text_chunk chunk)
    (process_text_chunk chunk interface prev_data_chunk)
    prev_data_chunk))

(defn process_file
  ([reader interface]
   (process_file reader interface "" {}))

  ([reader interface item prev_data_chunk]
   (let [line (.readLine reader)]
     (cond
       (nil? line) (process_item item interface prev_data_chunk)
       (separator? line) (let [new_data_chunk (process_item item interface prev_data_chunk)]
                           (recur reader interface "" new_data_chunk))
       :else (recur
              reader
              interface
              (str item "\n" line)
              prev_data_chunk)))))

(defn processing
  [file interface]
  (with-open [reader (io/reader file)]
    (process_file reader interface)))

(defn process_file_by_lines
  [file interface]
  (trc/trace-ns 'net-stats.file-by-lines)
  (processing file interface))

