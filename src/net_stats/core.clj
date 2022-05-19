(ns net-stats.core
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   )
  (:gen-class))

(def cli-options
  ;; An option with a required argument
  [["-i" "--interface IFACE" "interface name"
    :validate [string? "Must be string"]]
   ["-f" "--file FILE" "input file"
    :validate [string? "Must be string"]]

   ;; A non-idempotent option

   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(def iface "tun0")

(defn get_file
  [name]
  (slurp name))

(defn get_chunks
  [text]
  (str/split text #"(?sim)^--- net ---"))

(defn extract_timestamp
  [chunk]
    (->> chunk
       (str/split-lines)
       (filter #(re-find #"(?m)^\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d" %))
       (first)))

(defn parse_timestamp
  [text]
  (let [parser (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:SSzzz")]
    (.parse parser text)))

(defn get_timestamp
  [chunk]
  (let [text_timestamp (extract_timestamp chunk)]
    (parse_timestamp text_timestamp)))

(defn get_interface_content
  [chunk]
  (->> chunk
       (str/split-lines)
       (filter #(re-find (re-pattern iface) %))
       (first)))

(defn parse_interface_content
  [text]
  (json/read-str text))

(defn is_interface_matched
  [iface iface_data]
  (let [iface_in_data (get iface_data "ifname")]
    (= iface iface_in_data)))

(defn find_interface_part
  [iface all_interface_data]
  (->> all_interface_data
       (filter #(is_interface_matched iface %))
       (first)))

(defn extract_one_interface_data
  [iface all_interface_data]
  (let [iface_data (find_interface_part iface all_interface_data)
        rx_bytes (get-in iface_data ["stats64" "rx" "bytes"])
        tx_bytes (get-in iface_data ["stats64" "tx" "bytes"])]
    {:iface iface
     :rx rx_bytes
     :tx tx_bytes}))

(defn get_data_from_chunk
  [chunk]
  (let [
        timestamp (get_timestamp chunk)
        text (get_interface_content chunk)
        all_interface_data (parse_interface_content text)
        data (extract_one_interface_data iface all_interface_data)]
    {:ts timestamp
     :data data}))

(defn remove_empty_chunks
  [chunks]
  (filter #(not (str/blank? %)) chunks))

(defn build_data_chunks
  [chunks]
  (->> chunks
       (remove_empty_chunks)
       (map get_data_from_chunk)))

(defn get_data_for_interface
  [data_chunk interface]
  (let [timestamp (:timestamp data_chunk)
        rx (get-in data_chunk [interface :rx])
        tx (get-in data_chunk [interface :tx])]
    {:timestamp timestamp
     :rx rx
     :tx tx}))

(defn filter_by_interface
  [data_chunks interface]
  (map #(get_data_for_interface % interface) data_chunks))

(defn sort_by_datetime
  [data_chunks]
  (throw "not implemented"))

(defn get_data
  [name interface]
  (let [text (get_file name)
        chunks (get_chunks text)
        data_chunks (build_data_chunks chunks)
        specific_chunks (filter_by_interface data_chunks interface)
        sorted (sort_by_datetime specific_chunks)]
    sorted))

(defn -main
  "I don't do a whole lot."
  [& args]
  (let [
        opts (parse-opts args cli-options)
        file (get-in opts [:options :file])
        ]
    (println opts)
    (println file)
    (get_data file)
    )
  (println "Hello, World!"))
