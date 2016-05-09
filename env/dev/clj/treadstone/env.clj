(ns treadstone.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [treadstone.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[treadstone started successfully using the development profile]=-"))
   :middleware wrap-dev})
