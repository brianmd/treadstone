(ns murphydye.websockets.chatr
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [murphydye.websockets.core :as ws]
            [murphydye.websockets.stress-test :as stress]
            [murphydye.websockets.dbexplorer.core :as dbexplorer]
            [murphydye.window :as win]
            [murphydye.websockets.router :as router]
            ))


(defrecord person [id name])
(defrecord room [id name people owner])

(defonce messages (r/atom []))
(defonce rooms (r/atom {}))

(defn new-room [action]
  )

(defn send-message [action m]
  (println "send-message " action m)
  (ws/send-transit-msg!
   [:chatr action m]))
   ;; {:app :chat :action :speak :message msg}))

;; (defn message-list []
;;   [:ul
;;    (for [[i message] (map-indexed vector (reverse @messages))]
;;      ^{:key i}
;;      [:li message])])
(defn message-list [m]
  (println "in message-list")
  (println m)
  [:ul
   (for [[i message] (map-indexed vector (reverse (:messages @m)))]
     ^{:key i}
     [:li message])])

(defn message-input [m]
  (let [value (r/atom nil)]
    (.log js/console "new message-input")
    (fn []
      [:input.form-control
       {:type :text
        :placeholder "type in a message and press enter"
        :value @value
        :on-change #(do
                      (.log js/console (str @value ":changed:" (-> % .-target .-value)))
                      (reset! value (-> % .-target .-value)))
        :on-key-down
        #(when (= (.-keyCode %) 13)
           (.log js/console (str "submitting:" @value))
           (println "rooms" rooms)
           (println @m)
           (send-message :add-msg {:room-id (:id @m) :message @value})
           (reset! value nil))}])))

(defn add-row [click-fn content]
  [:div.row
   [:div.col-xs-12 {:on-click click-fn}
    content]])

(defn add-highlighted-row [content]
  [:div.row
   [:div.col-xs-12 {:style {:width "100%" :background-color "#ddd" :text-align :center}}
    content]])

(defn chatr-outbound-component []
  [:div.container-fluid
   [:div.row
    [:div.col-xs-12 {:style {:width "100%" :background-color "#ddd" :text-align :center}}
     [:b "Your Chats"]]]
   [add-highlighted-row [:b "Requesting Help"]]
   [add-row #(js/alert "clicked") "conn-1"]
   [add-row #(js/alert "clicked") [:i "conn-1"]]
   [:div.row
    [:div.col-md-12
     "somebody"]]
    [:div.row
     [:div.col-md-12
      "else"
      ]]
   ;; [:div.row
   ;;  [:div.col-md-12
   ;;   [:h2 "Unattended"]
   ;;   "somebody"]]
   ;; [:div.row
   ;;  [:div.col-md-12
   ;;   [:h2 "Permanent"]
   ;;   "somebody"]]
   ])

(defn chatr-component [m]
  [:div.container
   [:div.row
    [:div.col-md-12
     ;; [:h2 "Summit Chat"]
     ]]
   [:div.row
    [:div.col-md-4
     ;; [:input {:type "button" :value "Connect Websocket"
     ;;          :on-click #(init!)}]
     ]]
   [:div.row
    [:div.col-sm-6
     [message-input m]]]
   [:div.row
    [:div.col-sm-6
     [message-list m]]]
   ])



(defn new-client-window [f m]
  (let [win-id (win/new-window f m)]
    (ws/new-client {:win-id win-id})
    ))

(defn open-chatr! [m]
  (println "open-chatr!:" m)
  (new-client-window (chatr-component (reaction (@rooms (:room-id m)))) {:title "Summit Chat" :x 50 :y 100 :width 400 :height 400}))

(defn open-admin-chatr! [m]
  (win/new-window chatr-outbound-component {:title "Outbound Chatr" :x 50 :y 100 :width 400 :height 400})
  )

(defn update-room! [m]
  (swap! rooms assoc (:id m) m))

(defn update-messages! [m]
  (println "in update-messages!")
  (println m)
  (.play (js/Audio. "http://murphydye.com/bottleopen.mp3"))
  (let [id (:room-id m)]
    (swap! rooms assoc-in [id :messages] (conj (get-in @rooms [id :messages]) (:message m)))))

(defn notify-all-rooms! [state v])

(defn handle-chatr-action! [action-name m]
  (println action-name m)
  (case action-name
    :add-message (update-messages! m)
    :open-chatr (open-chatr! m)
    :open-admin-chatr (open-admin-chatr! m)
    :update-room (update-room! m)
    ))

(def chatr-actor (router/make-actor (atom {})))

(router/add chatr-actor :notify-all-rooms notify-all-rooms!)


(defn handle-action! [[app-name action-name m]]
  (println "\n\n\nin handle-action! ")
  (println "app: " app-name ", action-name: " action-name ", map: " m)
  (case app-name
    :global (ws/handle-action! action-name m)
    :chatr (handle-chatr-action! action-name m)
    :stress-test (stress/handle-action! action-name m)
    :dbexplorer (dbexplorer/handle-action! action-name m)
    else (println "bad action")
    ))

(defn init! []
  (win/qgrowl "creating websocket")
  (win/qgrowl (.-host js/location))
  (ws/make-websocket! (str "ws://" (.-host js/location) "/ws") handle-action!)
  ;; (mount-components)
  )
