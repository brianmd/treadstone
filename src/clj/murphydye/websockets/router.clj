(ns murphydye.websockets.router
  (:require [clojure.tools.logging :as log]
            [murphydye.utils.core :refer :all]
            ))

(defprotocol Dispatcher
  (act [state action-name m])
  (dispatch [this v])
  (add [this key func]))

(def ^:dynamic *router-ancestors* '())

(defn plain-dispatch [this v]
  ;; (log/info "about to dispatch in router " v)
  (let [app-or-action-name (first v)
        msg                (rest v)
        command            (first msg)
        actor?             (or (map? command) (nil? command))
        sub-dispatcher     ((deref (:routes this)) app-or-action-name)
        ]
    (log/info "about to dispatch in router:" (:title this) ", app-or-action-name:" app-or-action-name ", command:" command ", msg:" msg ", actor?:" actor? ", sub-dispatcher type:" (type sub-dispatcher))
    (if-not sub-dispatcher
      (throw (Exception. (str "no such app-name " app-or-action-name))))
    (binding [*router-ancestors* (conj *router-ancestors* app-or-action-name)]
      (if actor?
        (sub-dispatcher (:state this) app-or-action-name command)
        (dispatch sub-dispatcher msg))
      )))

(defrecord Router [title routes state]
  Dispatcher
  (dispatch [this v]
    (plain-dispatch this v))
  (add [this key val]
    (swap! routes assoc key val)
    nil   ;; because above is not printable
    ))

(defn make-router
  ([name] (make-router name nil))
  ([name state] (->Router name (atom {}) state)))



;; (defonce root-router (make-router "root" (atom {:connections {}})))
(defonce root-router (atom (make-router "root" (atom {:connections {}}))))

;; see timer.clj for example

(examples
 (keys (deref (:routes root-router)))
 (keys @(:routes root-router))
 (:stress-test @(:routes root-router))
 (keys @(:actions (:stress-test @(:routes root-router))))
 )

