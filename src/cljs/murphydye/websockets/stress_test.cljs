(ns murphydye.websockets.stress-test
  (:require [reagent.core :as r]
            ;; [re-com.core   :refer [h-box v-box box gap line input-text input-textarea label checkbox radio-button slider title p]]
            [murphydye.websockets.core :as ws]
            [murphydye.window :as win :refer [alert]]
            [murphydye.websockets.router :as router]
            ))

(defonce client-seq-num (atom 0))

(defn send-message [msg]
  (ws/send-transit-msg! msg))
;; [:stress-test action msg]))

(defn handle-action! [action-name m]
  ;; (win/alert [action m])
  (case action-name
    :iteration (send-message [:stress-test :received m])
    :finished (win/static-alert (select-keys m [:missing :latency]))
    (win/growl {:message [:stress-test-error action-name m] :delay :sticky :status :error})))

(defn start-stress-test [num-threads num-iterations]
  (let [client-id (swap! client-seq-num inc)]
    ;; (win/alert (str client-id ":" num-threads "-" num-iterations))
    (send-message [:stress-test :start-test {:client-id client-id :num-threads num-threads :num-iterations num-iterations}])))

(defn label [value]
  ;; [:div.col-md-4.bold.right-justify value])
  [:div.col-md-6.bold value])

(defn integer-input [value]
  [:div.col-md-6
   [:input {:type "number"
            :value @value
            :on-change #(reset! value (-> % .-target .-value))}]])

(defn stress-test-component []
  (let [
        num-threads (r/atom "1")
        num-iterations (r/atom "5")
        ]
    [:div.container-fluid.wide
     [:div.row
      [label "Number of threads:"]
      [integer-input num-threads]
      ]
     [:div.row
      [label "Total iterations:"]
      [integer-input num-iterations]
      ]
     [:div.row [:div.col-md-12 "."]]
     [:div.row
      [:div.col-md-6]
      [:div.col-md-6
       [:input.green {:type "button" :value "Stress Me"
                      :on-click #(do
                                   (win/alert "i'm stressing ...")
                                   (start-stress-test
                                    (int @num-threads)
                                    (int @num-iterations)))}]]]
     ;; [:div.row
     ;;  [:div.col-xs-4 [label :label "Total  iterations"]]
     ;;  [:div.col-xs-4 [input-text :model num-iterations
     ;;                  :on-change #(reset! num-iterations %)]]
      ;; ]
     ]))

