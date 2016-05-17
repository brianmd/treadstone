(ns user
  (:require [mount.core :as mount]
            [treadstone.figwheel :refer [start-fw stop-fw cljs]]
            treadstone.core))

(defn start []
  (mount/start-without #'treadstone.core/repl-server))

(defn stop []
  (mount/stop-except #'treadstone.core/repl-server))

(defn restart []
  (stop)
  (start))

(in-ns 'treadstone.core)
(println "\n\nto start webserver:\n  (in-ns 'treadstone.core)\n  (-main)\n\nhttp://localhost:7777/#/about\n\n")


