;; component contains buttons which open other components in windows

(ns murphydye.components
  (:require [reagent.core :as r]
            [murphydye.websockets.core :as ws]
            [murphydye.window :as win]
            [murphydye.websockets.chatr :as chatr]
            [murphydye.websockets.stress-test :refer [stress-test-component]]
            [murphydye.websockets.dbexplorer.core :refer [dbexplorer-component]]
            [murphydye.websockets.router :as router]
            ))

(defn new-client-window [f m]
  (let [win-id (win/new-window f m)]
    (ws/new-client {:win-id win-id})
    ))
  ;;               chatr/chatr-outbound-component
  ;;               {:title "Outbound Chatr" :x 50 :y 100 :width 400 :height 400})])}]
  ;; )

(defn select-list [items name-fn options]
  )
(defn components []
  (win/qgrowl "starting components")
  (let [v (r/atom nil)]
    (fn []
      [:span.wide
       [:input.green {:type "button" :value "Db Explorer"
                :on-click #(win/new-window dbexplorer-component {:title "Db Explorer" :x 50 :y 100 :width 200 :height 200})}]
       [:input.purple {:type "button" :value "Outbound Chat"
                :on-click #(win/new-window chatr/chatr-outbound-component {:title "Outbound Chatr" :x 50 :y 100 :width 400 :height 400})}]
       [:input.purple {:type "button" :value "Customer Chat"
                ;; :on-click #(win/new-window chatr/chatr-component {:title "Summit Chat" :x 50 :y 100 :width 400 :height 400})}]
                :on-click #(new-client-window chatr/chatr-component {:title "Summit Chat" :x 50 :y 100 :width 400 :height 400})}]
       [:br]
       [win/dialog-test]
       [:input {:type "button" :value "Websocket Stress Test"
                :on-click #(win/new-window stress-test-component {:title "Websocket Stress Test" :x 50 :y 150 :width 400 :height 200 :scrollable false})}]
       ]
      )))

