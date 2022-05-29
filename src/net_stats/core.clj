(ns net-stats.core
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [net-stats.file-by-lines :as file_by_lines]
   [net-stats.whole-file :as whole_file])
  (:gen-class))

(def whole_file_mode "whole_file")
(def file_by_lines_mode "file_by_lines")

(def cli-options
  ;; An option with a required argument
  [["-i" "--interface IFACE" "interface name"
    :validate [string? "Must be string"]]
   ["-f" "--file FILE" "input file"
    :validate [string? "Must be string"]]
   ["-m" "--mode MODE" (str "processing mode: '" whole_file_mode "' or '" file_by_lines_mode "'")
    :default whole_file_mode
    :validate [string? "Must be string"]]

   ;; A non-idempotent option

   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)
        mode (get-in opts [:options :mode])
        file (get-in opts [:options :file])
        interface (get-in opts [:options :interface])]
    ;; (println opts)
    (cond
      ;; (:help opts) (usage)
      (= mode whole_file_mode) (whole_file/process_whole_file_at_once file interface)
      (= mode file_by_lines_mode) (file_by_lines/process_file_by_lines file interface)
      :else nil)))
