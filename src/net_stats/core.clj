(ns net-stats.core
  (:require
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

(defn get_file
  [name]
  (slurp name))

(defn get_chunks
  [text]
  (str/split text #"(?sim)^--- net ---"))

(defn extract_timestamp
  [chunk]
  (re-find #"(?m)^.* May [ :\w]* 2022$" chunk))

(defn parse_timestamp
  [text]
  (let [parser (java.text.SimpleDateFormat. "EEE MMM dd HH:mm:SS zzz yyyy")]
    (.parse parser text)))

(defn get_timestamp
  [chunk]
  (let [text_timestamp (extract_timestamp chunk)
        parser (java.text.SimpleDateFormat. "EEE MMM dd HH:mm:SS zzz yyyy")]
    (parse_timestamp text_timestamp)))

(defn get_data_from_chunk
  [chunk]
  (let [
        timestamp (get_timestamp chunk)
        ]
    timestamp
    )
  (throw "not implemented"))

(defn build_data_chunks
  [chunks]
  (map get_data_from_chunk chunks))

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
