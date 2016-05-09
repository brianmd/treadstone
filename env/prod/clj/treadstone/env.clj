(ns treadstone.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[treadstone started successfully]=-"))
   :middleware identity})
