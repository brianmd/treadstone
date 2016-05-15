(ns murphydye.websockets.dbexplorer
  (:require 
            ;; [korma.core :as k]
            [korma.db :as kdb]

            [clojure.java.jdbc :as j]

            [murphydye.utils.core :refer :all]
            [murphydye.websockets.router :as r :refer [add]]
            [murphydye.websockets.core :as ws]
            [murphydye.websockets.dbexplorer-dbs :as db-env]
            ;; [summit.step.restapi :as restapi]
            ;; [summit.db.step :refer [get-golden-product]]

            [clojure.tools.logging :as log]))

"
from client:
  :databases
  :tables {:dbname}
"

(def actor-name :dbexplorer)

(defonce db-pools (atom {}))
(defn db-configs []
  (:dbs db-env/db-list))
(defn connection-fn [type]
  (case (keyword type)
    :mysql kdb/mysql
    :oracle kdb/oracle))
(defn make-connection [dbname]
  (let [config (dbname (db-configs))]
    ((connection-fn (:subprotocol config)) config)))
(defn connection-for [dbname]
  (if-let [conn (dbname @db-pools)]
    conn
    (swap! db-pools assoc dbname (make-connection dbname))))
(defn query [sql]
  (jquery (connection-for :bh-local) sql))

(defn convert [m]
  (let [n (:dbname m)]
    [n {:name n :type (:subprotocol m)}]))

(defn databases []
  (into {} (map!
        (fn [[k m]] (convert (assoc m :dbname k)))
        (db-configs))))

(defn mysql-tables [dbname]
  (let [conn (connection-for (keyword dbname))]
    (j/query conn
             ["select table_name from information_schema.tables where table_schema=? order by table_name"
              (->str (:dbname conn))
              ])))
;; (mysql-tables :bh-local)
;; (mysql-tables "bh-local")

(defn mysql-table [dbname table-name]
  (let [conn (connection-for dbname)
        data (j/query conn
                      ;; ["select * from ? limit 2" table-name]
                      [(str "select * from " table-name " limit 2")]
                      ;; {:as-arrays? true}
                      )]
    data))

;; (mysql-table :bh-local "customers")



(defn send-message [action m]
  (let [v [actor-name action m]]
  ;; (let [v (vec (conj (seq msg) actor-name))]
    (ws/send-to-connection ws/*connection* v)
    ;; (println v)
    ))

(defn databases! [_ _ _]
  (let [d (databases)]
    (send-message :databases {:databases (databases)})))
;; (database-names! nil nil)

(defn tables! [_ _ m]
  (println "tables ! " m)
  (let [names (mysql-tables (:dbname m))]
    ;; (println names)
    (send-message :tables {:dbname (:dbname m) :tables names})
    ))
;; (tables! nil [:tables {:dbname :bh-local}])

(defn table! [_ [_ m]]
  (let [data (mysql-table (:dbname m) (:table-name m))]
    (send-message :table (assoc m :data data))))

(defn create-actor [actor-name]
  (let [actor (r/make-router actor-name (atom {}))]
    (add actor :databases databases!)
    (add actor :tables tables!)
    (add actor :table table!)
    actor
    ))

;; (def dbexplorer-actor (r/make-router "dbexplorer" (atom {})))

;; (add dbexplorer-actor :databases databases!)
;; (add dbexplorer-actor :tables tables!)
;; (add dbexplorer-actor :table table!)
