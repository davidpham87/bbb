(ns jetdotter.db
  (:require
   [babashka.fs :as fs]
   [datalevin.core :as d]))

(def get-conn d/get-conn)
(def transact! d/transact!)
(def q d/q)
(def close d/close)

(def schema {:hash {:db/valueType :db.type/string}
             :filename {:db/valueType :db.type/string
                        :db/unique :db.unique/identity}})

(defn make-db! [path]
  (d/get-conn path schema))

(defn conn []
  (d/get-conn "/tmp/jetdotter/mydb"))
