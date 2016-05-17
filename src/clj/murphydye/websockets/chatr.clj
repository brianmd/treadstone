(println "loading murphydye.websockets.chatr")

(ns murphydye.websockets.chatr
  (:require [clojure.tools.logging :as log]
            [immutant.web.async :as async]
            [cognitect.transit :as transit]

            [murphydye.utils.core :as utils]
            [murphydye.websockets.router :as r :refer [add]]
            [murphydye.websockets.core :as ws]
            ))

(defonce rooms-seq-num (atom 0))
(defonce message-seq-num (atom 0))
;; (defonce rooms (atom {}))

(defrecord RoomConnectors [room-id remote-id])
(defrecord Room [id status name connection-ids messages owner])
(defrecord Message [id sent-by-connection-id sent-time text])
;; ;status:  :permanent :waiting :assisting :disconnected :closed

(defn make-room [connection]
  (let [id (swap! rooms-seq-num inc)
        room (->Room id :waiting (str "room-" id) [(:id connection)] [] nil)]
    room))
;; (make-room {:id 3})

(defn make-message [connection msg]
  (let [conn-id (:id connection)]
    (->Message (swap! message-seq-num inc) conn-id (java.util.Date.) msg)))

(defn notify-room! [state [_ room-id msg]]
  )

(defn notify-all-admins! [state [_ room-id msg]]
  )

(defn send-add-message
  ([m] (fn [conn] (send-add-message conn m)))
  ([conn m]
   (println "send-add-message:" m)
   (try
     (ws/send-to-connection conn [:chatr :add-message m])
     (catch Exception e (log/error (str "send-add-message caught exception: " (.getMessage e)))))
   ))




(defn connection-by-id [id]
  (println "\n\nconnections" @ws/connections "\n\n")
  (first (filter #(= id (:id %)) (vals @ws/connections))))



(defn add-msg! [state _ m]
  (log/info "in add-msg: " (pr-str m))
  (println "\n\nstate:" state)
  (println "m:" m)
  (let [ids (get-in @state [:rooms (:room-id m) :connection-ids])
  ;; (let [ids (:connection-ids state)
        conns (map #(connection-by-id %) (set ids))
        text (:message m)
        msg (make-message ws/*connection* (:message m))
        m (assoc m :message msg)]
    (swap! state update-in [:rooms (:room-id m) :messages] conj msg)
    (println "ids:" ids)
    (println "\n\nm:" m)
    (utils/map! (send-add-message m) conns)))
  ;; (map! #(ws/send-to-connection (@ws/connections %) [:chatr :add-message m])
  ;;       (try
  ;;         (ws/send-to-connections (vals @ws/connections) [:chatr :add-message m])
  ;;         (catch Exception e (log/error (str "add-msg! caught exception: " (.getMessage e)))))))


(defn notify-all-rooms! [state _ m]
  (log/info "in notify-all-rooms " m)
  (try
    (ws/send-to-connections (vals @ws/connections) m)
    (catch Exception e (log/error (str "notify-all-rooms! caught exception: " (.getMessage e)))))
  )

(defn request-admin-chatr! [state _ m]
  (ws/send-to-self [:chatr :open-admin-chatr {}]))

(defn request-chatr! [state _ m]
  (println "in request-chatr!" m)
  (let [room (make-room ws/*connection*)
        room-id (:id room)]
    (swap! state assoc-in [:rooms room-id] room)
    (ws/send-to-connections (vals @ws/connections) [:chatr :update-room room])
    (ws/send-to-self [:chatr :open-chatr {:room-id room-id}])
    ))

(defn connect-to-room! [state _ m]
  (println "in connect-to-room!")
  (println m)
  (swap! state update-in [:rooms (:room-id m) :connection-ids] conj (:id ws/*connection*))
  (println "\n\nstate" state)
  (let [room (get-in @state [:rooms (:room-id m)])
        room-id (:id room)
        ]
    (println "room" room)
    (ws/send-to-self [:chatr :update-room room])
    (println "updated room")
    (ws/send-to-self [:chatr :open-chatr {:room-id room-id}])
    (println "opened chatr")
    (add-msg! state nil {:room-id room-id :message "How may we help you?"})
    (println "added msg")
    ))

(defn request-help [state v]
  (let [room (make-room state v)]
    (notify-all-admins! state room)))

(defn create-actor [actor-name]
  (let [actor (r/make-router actor-name
                                   (atom {:rooms {}
                                          :people {}}))]
    (add actor :add-msg add-msg!)

    (add actor :request-help identity)
    (add actor :make-room identity)   ;; [:make-room [who-to-invite ...] [who-to-auto-open ...(?)]]
    (add actor :inactive-room identity)
    (add actor :close-room identity)
    (add actor :notify-room identity)
    (add actor :notify-all-rooms notify-all-rooms!)
    (add actor :add-connection identity)
    (add actor :remove-connection identity)
    (add actor :room-list identity)
    (add actor :rooms-waiting-for-outbound identity)
    (add actor :request-admin-chatr request-admin-chatr!)
    (add actor :request-chatr request-chatr!)
    (add actor :connect-to-room connect-to-room!)
    actor))


(println "done loading murphydye.websockets.chatr")
"     server side
one web socket handler per server
    has many connections
    has one router
        has many apps

each connection
    has one web socket channel
    has one customer (some of which are outbound)
    has one browser
    has many clients (often associated w/ windows in the browser)

common messages to apps:
- new/update-connection :conn-id
- connection-closed :conn-id
- new-client :conn-id :client-id (client-id comes from client)
- client-closed :conn-id :client-id

one chatr app
   has multiple rooms
       has many clients
       has many messages
   multiple chatr windows per connection

chatr messages:
- set-rooms  (sends only vectors of :my-rooms, :unattended, :other, not clients/msgs)
- set-room-messages
- set-room-clients
"

"     client side

common client web socket comamands:
- new-client
- client-closed

one chatr window
   has one room
      has many clients
      has many messages

one admin chatr window
   has many my-rooms
   has many unattended-rooms
   has many other-admin-rooms
   note: admin chatr has no clients or messages
"

