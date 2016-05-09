(ns murphydye.websockets.router
  (:require ;; use first for clojurescript, second for clojure
            [murphydye.window :as win]
            ;; [clojure.tools.logging :as log]
            ))
;; these two methods change between clojure and clojurescript
(if true
  ;; for clojurescript
  (do
    (def log-notice win/qgrowl)
    (def error-class js/Error))
  ;; for clojure
  ;; (do
  ;;   (def notifier log/info)
  ;;   (def error-class Exception.))
  )


(defprotocol Dispatcher
  (dispatch [this v])
  (add [this key func]))

(def ^:dynamic *router-ancestors* '())

(defrecord Router [routes]
  Dispatcher
  (dispatch [this v]
    (log-notice "about to dispatch in router " v)
    (let [app-name (first v)
          sub-dispatcher ((deref (:routes this)) app-name)
          ]
      (if-not sub-dispatcher
        (throw (error-class. (str "no such app-name " app-name))))
      (binding [*router-ancestors* (conj *router-ancestors* app-name)]
        (dispatch sub-dispatcher (rest v)))))
  (add [this key val]
    (swap! routes assoc key val)))

(defn make-router [] (->Router (atom {})))



(defrecord Actor [actions state]
  Dispatcher
  (dispatch [this event]
    (log-notice "Actor.dispatch " event)
    (let [action-name (first event)
          func ((deref (:actions this)) action-name)]
      (if-not func
        (throw (error-class. (str "no such action " action-name))))
      (func state event)))
  (add [this event-name func] (swap! (:actions this) assoc event-name func)))

(defn make-actor [initial-state] (->Actor (atom {}) initial-state))


(def root-router (make-router))

;; see timer.clj for example

