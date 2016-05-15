(ns murphydye.websockets.core
  (:require [compojure.core :refer [GET defroutes wrap-routes]]
            [clojure.tools.logging :as log]
            [immutant.web.async :as async]
            [cognitect.transit :as transit]

            [murphydye.utils.core :as utils]
            [murphydye.websockets.router :as r :refer [add dispatch]]
            [murphydye.websockets.router :as r]
            )
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]
           [java.nio.charset StandardCharsets]
           ))

(log/info "in websockets.cljs")

(def ^:dynamic *connection* nil)

(defonce connections (atom {}))
(defonce connection-seq-num (atom 0))
(defonce customer-seq-num (atom 0))
(defonce client-seq-num (atom 0))

(defn make-customer []
  (let [seq-num (swap! customer-seq-num inc)]
    (atom {:id nil
           :seq-num seq-num
           :name (str "seq-num-" seq-num)
           :clients {}
           })))

(defn make-connection [channel]
  (let [connection-num (swap! connection-seq-num inc)]
  ;; (let [chan-num (channel-id channel)]
    {:id connection-num
     :name (str "chan-" connection-num)
     :channel channel
     :customer (make-customer)
     :ip-address 3
     :browser-info 3
     :created-at (java.util.Date.) 
     }))

(defn msg->transit [m]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer m)
    (.toString out)))

(defn transit->msg [t]
  (let [in (ByteArrayInputStream. (byte-array (map byte t)))
        reader (transit/reader in :json)]
    (transit/read reader)))


(defn send-to-connection [conn msg]
  (async/send! (:channel conn) (msg->transit msg)))

(defn send-to-self [msg]
  (send-to-connection *connection* msg))

(defn send-to-connections [connections msg]
  (doseq [conn connections]
    (log/info conn)
    (async/send! (:channel conn) (msg->transit msg))))

(defn connect! [channel]
  (log/info channel)
  (log/info "---------")
  ;; (log/info (System/identityHashCode. channel))
  (log/info (System/identityHashCode channel))
  (let [conn (make-connection channel)]
    (log/info (str "channel connected " (:name conn)))
    (log/info (:id conn))
    ;; (swap! connections assoc (:id conn) conn)
    (swap! connections assoc channel conn)
    (log/info "---------- associated")
    channel))

(defn disconnect! [channel {:keys [code reason]}]
  (log/info "close code:" code "reason:" reason)
  (log/info "closing channel " channel)
  (log/info "   channel name " (:name (@connections channel)))
  (swap! connections dissoc channel)
  )

(defn dispatch-message! [channel msg]
  (log/info "about to dispatch message")
  (try
    (log/info "dispatching message ... " msg)
    (let [connection (@connections channel)
          message (transit->msg msg)]
      (log/info (.getClass msg))
      (log/info message)
      (log/info channel)
      (log/info msg)
      (log/info "adding a message")
      ;; (async/send! channel (msg->transit {:message "got it"}))
      (binding [*connection* connection]
        (log/info "msg" msg)
        (r/dispatch @r/root-router message))
      ;; (doseq [conn (vals @connections)]
      ;;   (async/send! (:channel conn) message))
      )
    (catch Exception e (log/error (str "caught exception during websocket dispatch-message!: " (.getMessage e)))))
  )

#_(defn notify-clients! [channel msg]
  (try
    (do
      (log/info "notifing clients ..")
      (log/info (.getClass msg))
      (log/info (transit->msg msg))
      (log/info channel)
      (log/info msg)
      (log/info "adding a message")
      ;; (async/send! channel (msg->transit {:message "got it"}))
      (doseq [conn (vals @connections)]
        (async/send! (:channel conn) msg))
      )
    (catch Exception e (log/error (str "caught exception: " (.getMessage e)))))
    )

(def websocket-callbacks
  "WebSocket callback functions"
  {:on-open connect!
   :on-close disconnect!
   ;; :on-message notify-clients!
   :on-message dispatch-message!
   })

(defn ws-handler [request]
  (log/info "ws-handler")
  (async/as-channel request websocket-callbacks))

(defroutes websocket-routes
  (GET "/ws" [] ws-handler))

(defn handle-new-client [])


(log/info "done loading websockets.cljs")


;; auto reload from a repl would be bad. But if eval in emacs, good to go
(log/info "(in-ns 'treadstone.core)")
(log/info "(-main)")


(utils/examples
 (send-to-connections (vals @connections) [:chatr :add-message {:message (str (utils/now))}])
 (send-to-connections (vals @connections) [:global :notify {:message (str (utils/now))}])
 (send-to-connections (vals @connections) [:chatr :open-chatr {:message (str (utils/now))}])
 )
