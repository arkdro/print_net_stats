(ns net-stats.file-by-lines
  (:require
   [clojure.java.io :as io]
   [clojure.tools.trace :as trc]
   [net-stats.item-util :as item_util]))

(defn separator?
  [line]
  (= line item_util/separating_line))

(defn process_text_chunk
  [chunk interface prev_data_chunk]
  (let [data_chunk (item_util/get_data_from_chunk chunk interface)
        ]
    ))

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

