(ns jetdotter.db
  (:require
   [babashka.fs :as fs]
   [datalevin.core :as d]))

(def get-conn d/get-conn)
(def transact! d/transact!)
(def q d/q)
(def close d/close)

(defn conn []
  (d/get-conn "/tmp/jetdotter/mydb"))
