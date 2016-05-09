(println "loading murphydye.components")

;; This namespace references all the other components
;; The application can reference solely this namespace
;; without concern for cyclical dependencies

(ns murphydye.components
  (:require ;
            [murphydye.websockets.core :as ws]
            [murphydye.websockets.router :as r]
            [murphydye.websockets.chatr :as chatr]
            [murphydye.websockets.stress-test :as stress-test]
            [murphydye.websockets.dbexplorer :as dbexplorer]
            ))


(do
  (r/add r/root-router :chatr chatr/chatr-actor)
  (r/add r/root-router :stress-test stress-test/stress-test-actor)
  (r/add r/root-router :websocket ws/websocket-actor)
  (r/add r/root-router :dbexplorer dbexplorer/dbexplorer-actor)
  ;; the result of the above is unable to be printed, so if want to run in a repl, need to return nil
  nil)


(println "done loading murphydye.components")
