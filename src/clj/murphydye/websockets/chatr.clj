(println "loading murphydye.websockets.chatr")

(ns murphydye.websockets.chatr
  (:require [clojure.tools.logging :as log]
            [immutant.web.async :as async]
            [cognitect.transit :as transit]

            [murphydye.websockets.router :as r :refer [add dispatch]]
            [murphydye.websockets.core :as ws]
            ))

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

(defonce rooms-seq-num (atom 0))
(defonce message-seq-num (atom 0))

(defrecord RoomConnectors [room-id remote-id])
(defrecord Room [id name connectors owner])
(defn make-room [connection]
  (let [id (swap! rooms-seq-num inc)
        room (->Room id (str "room-" id) [(:id connection)] nil)]
    room))
;; (make-room {:id 3})


(defn notify-room! [state [_ room-id msg]]
  )

(defn notify-all-admins! [state [_ room-id msg]]
  )

(defn add-msg! [state _ m]
  (log/info "in add-msg: " (pr-str m))
  (try
    (ws/send-to-connections (vals @ws/connections) [:chatr :add-message m])
    (catch Exception e (log/error (str "add-msg! caught exception: " (.getMessage e))))))

(defn notify-all-rooms! [state _ m]
  (log/info "in notify-all-rooms " m)
  (try
    (ws/send-to-connections (vals @ws/connections) m)
    (catch Exception e (log/error (str "notify-all-rooms! caught exception: " (.getMessage e)))))
  )

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
    actor))


(println "done loading murphydye.websockets.chatr")
