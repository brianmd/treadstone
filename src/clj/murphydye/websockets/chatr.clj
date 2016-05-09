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

(defn add-msg! [state [_ msg]]
  (log/info "in add-msg: " (pr-str msg))
  (try
    (ws/send-to-connections (vals @ws/connections) [:chatr msg])
    (catch Exception e (log/error (str "add-msg! caught exception: " (.getMessage e)))))
  )

(defn notify-all-rooms! [state [_ msg]]
  (log/info "in notify-all-rooms " msg)
  (try
    (ws/send-to-connections (vals @ws/connections) msg)
    (catch Exception e (log/error (str "notify-all-rooms! caught exception: " (.getMessage e)))))
  )


(defonce chatr-actor (r/make-actor (atom {:rooms {}
                                          :people {}})))

(defn request-help [state v]
  (let [room (make-room state v)]
    (notify-all-admins! state room)))

(add chatr-actor :add-msg add-msg!)

(add chatr-actor :request-help identity)
(add chatr-actor :make-room identity)   ;; [:make-room [who-to-invite ...] [who-to-auto-open ...(?)]]
(add chatr-actor :inactive-room identity)
(add chatr-actor :close-room identity)
(add chatr-actor :notify-room identity)
(add chatr-actor :notify-all-rooms notify-all-rooms!)
(add chatr-actor :add-connection identity)
(add chatr-actor :remove-connection identity)
(add chatr-actor :room-list identity)
(add chatr-actor :rooms-waiting-for-outbound identity)


(println "done loading murphydye.websockets.chatr")
