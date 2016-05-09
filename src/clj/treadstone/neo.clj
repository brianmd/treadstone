;; (ns treadstone.neo
;;   (:require [clojurewerkz.neocons.rest :as nr]
;;             [clojurewerkz.neocons.rest.nodes :as nodes]
;;             [clojurewerkz.neocons.rest.relationships :as relations]
;;             [clojurewerkz.neocons.rest.cypher :as cypher]

;;             [korma.core :as k]

;;             [user :as user]
;;             [treadstone.config :refer [env]]
;;             [treadstone.utils.core :refer :all]
;;             ))


;; ;; (user/restart)

;; ;; (clojure.pprint/pprint (-> env :database-url))
;; ;; (clojure.pprint/pprint (-> env :neo4j))

;; (examples
 
;;  (def neo4j-map (:neo4j env))
;;  (def conn (nr/connect (:url neo4j-map) (:username neo4j-map) (:pw neo4j-map)))

;;  (def node (nodes/create conn {:url "http://clojureneo4j.info" :domain "clojureneo4j.info"}))


;;  (cypher/tquery conn "MATCH ()-[r:FOLLOWS]->() RETURN r LIMIT 25")
;;  (ppn (cypher/tquery conn "MATCH (a)-[r:FOLLOWS]->() RETURN r,a LIMIT 2"))
;;  (ppn (cypher/tquery conn "MATCH (a)-[r:FOLLOWS]->() RETURN a,r LIMIT 2")))

