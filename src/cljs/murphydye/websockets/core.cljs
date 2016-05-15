(ns murphydye.websockets.core
  (:require [cognitect.transit :as t]
            [murphydye.window :as win]
            ;; [murphydye.websockets.router :as r]
            ))

(defonce temp-client-seq-num (atom 0))
(defonce temp-clients (atom {}))
(defonce clients (atom {}))

(defonce ws-chan (atom nil))  ;; websocket channel
(def json-reader (t/reader :json))
(def json-writer (t/writer :json))

(defn receive-transit-msg!
  [update-fn]
  (fn [msg]
    (let [data (->> msg .-data (t/read json-reader))]
      ;; (win/qgrowl (str "received websocket:" (pr-str data)))
      (.log js/console (str "received websocket-----------" (pr-str data)))
      (update-fn
       (->> msg .-data (t/read json-reader))))))

(defn send-transit-msg!
  [msg]
  (if @ws-chan
    (.send @ws-chan (t/write json-writer msg))
    (throw (js/Error. "Websocket is not available!"))))

(defn send-msg [msg]
  (send-transit-msg! msg))

(defn new-client [m]
  (let [seq-num (swap! temp-client-seq-num inc)]
    (swap! temp-clients assoc seq-num (assoc m :id seq-num))
    (send-msg [:global :new-client {:seq-num seq-num}])))

(defn set-client-id [temp-id id]
  (let [client (temp-clients temp-id)]
    (if client
      (do
        (swap! clients assoc id (assoc client :id (:id client)))
        (swap! temp-clients dissoc (:id client)))
      (throw (js/Error. (str "No such temporary client id: " temp-id))))))

(defn handle-action! [action-name m]
  (win/static-alert [action-name m]))

(defn make-websocket! [url receive-handler]
  (println "attempting to connect websocket")
  (if-let [chan (js/WebSocket. url)]
    (do
      (set! (.-onmessage chan) (receive-transit-msg! receive-handler))
      (reset! ws-chan chan)
      (println "Websocket connection established with: " url))
    (throw (js/Error. "Websocket connection failed!"))))


