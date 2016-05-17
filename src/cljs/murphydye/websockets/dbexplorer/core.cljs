(ns murphydye.websockets.dbexplorer.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r :refer [atom]]
            [clojure.string :as string]
            ;; [cljs.pprint :refer [pprint]]

            [murphydye.websockets.core :as ws]
            [murphydye.window :as win]
            [murphydye.websockets.router :as router]
            ))

;; (def app-state
;;   (r/atom
;;    {:contacts
;;     [{:first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}
;;      {:first "Alyssa" :middle-initial "P" :last "Hacker" :email "aphacker@mit.edu"}
;;      {:first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"}
;;      {:first "Louis" :last "Reasoner" :email "prolog@mit.edu"}
;;      {:first "Cy" :middle-initial "D" :last "Effect" :email "bugs@mit.edu"}
;;      {:first "Lem" :middle-initial "E" :last "Tweakit" :email "morebugs@mit.edu"}]}))

;; (defn update-contacts! [f & args]
;;   (apply swap! app-state update-in [:contacts] f args))

;; (defn add-contact! [c]
;;   (update-contacts! conj c))

;; (defn remove-contact! [c]
;;   (update-contacts! (fn [cs]
;;                       (vec (remove #(= % c) cs)))
;;                     c))

;; (defn parse-contact [contact-str]
;;   (let [[first middle last :as parts] (string/split contact-str #"\s+")
;;         [first last middle] (if (nil? last) [first middle] [first last middle])
;;         middle (when middle (string/replace middle "." ""))
;;         c (if middle (count middle) 0)]
;;     (when (>= (reduce + (map #(if % 1 0) parts)) 2)
;;       (cond-> {:first first :last last}
;;         (== c 1) (assoc :middle-initial middle)
;;         (>= c 2) (assoc :middle middle)))))


(defn send-message [action msg]
  (win/alert (str action msg))
  (ws/send-transit-msg!
   [:dbexplorer action msg]))



(def dbs (r/atom {}))
(defn add-database [name db]
  ;; (swap! dbs assoc name (r/atom db)))
  (swap! dbs assoc name db))

(defn find-db [name]
  (@dbs name))

(defn ensure-dbs []
  (when (empty? @dbs)
    (win/qgrowl "getting databases")
    (send-message :databases {})))

(defn ensure-tables [database-name]
  (when (nil? (@dbs database-name))
    (win/qgrowl "getting table names")))

;; (if-not (find-db "bh-local")
;;   (add-database "bh-local"
;;                 {:name "bh-local"
;;                  :type :mysql
;;                  :descript "Blue Harvest Database"
;;                  :tables [
;;                           {:name "customers"}
;;                           {:name "carts"}
;;                           {:name "accounts"}
;;                           {:name "line-items"}
;;                           {:name "permissions"}
;;                           ]}))
;; (if-not (find-db "step-prd")
;;   (add-database "step-prd"
;;                 {:name "step-prd"
;;                  :type "oracle"
;;                  }))

(defn view-clicked [table]
  (win/alert (str "view " (:name table)))
  false)

(defn cols-clicked [table]
  (win/alert (str "cols " (:name table)))
  false)

(defn ensure-table-data [db table]
  ;; (win/static-alert [db table])
  ;; (println [(:name @db) table])
  (send-message :table {:dbname (:name @db) :table-name (:table-name table)})
  )


(defn show-table-info [db table]
  (let [n (:table_name table)]
    [:p {:on-click #(win/alert (str "clicked" n))}
     n
     [:span.right-justify
      [:span.green.padded {:on-click #(view-clicked table)} "view"]
      [:span.red.padded {:on-click #(cols-clicked table)} "cols"]
      ]
     ;; [:div.btn-group.btn-group-sm {:role "group"}
     ;; [:button.btn.btn-success.btn-sm {:type "button"} "View"]
     ;;  [:span.glyphicon.glyphicon-align-left {:aria-hidden true}]]
     ]))


(defn show-table-component [db table]
  (ensure-table-data db table)
  )

(defn database-component [db]
  ;; (println db (first (:tables db)))
  (let [n (name (:name @db))
        tbls (reaction (:tables @db))]
    [:div.container-fluid.wide.list
     [:b.wide.text-center n " tables"]
     (for [tbl @tbls]
       ^{:key (:table_name tbl)}
       [show-table-info db tbl])
     ]))

(defn ensure-database [db]
  ;; (win/static-alert (str "ensure-database " (:name db)))
  (when (nil? (:tables db))
    (win/alert "sending :tables message")
    (send-message :tables {:dbname (:name db)}))
  )

(defn show-database-info [db]
  (println @db)
  (println (:name @db))
  (let [n (name (:name @db))]
    [:p
     n
     [:span.right-justify
      [:span.green.padded
       {:on-click #(do
                     (ensure-database @db)
                     (win/new-window (fn [] (database-component db))
                                     {:title n :x 260 :y 100 :width 350 :height 400}))}
       "view"]
      ]
     ]))

(defn dbexplorer-component []
  (ensure-dbs)
  ;; (let [db (find-db :bh-local)]
  [:div.container-fluid.wide
   [:b.wide.text-center "Databases"]
   [:div.list
    (for [db (vals @dbs)]
      ^{:key (:name db)}
      [show-database-info (reaction ((:name db) @dbs))])]])

(defn handle-action! [action-name m]
  ;; (win/alert (str "received: " [action m]))
  (println "received: " [action-name m])
  (case action-name
    :databases (do (println m) (reset! dbs (:databases m)))
    :tables (swap! dbs assoc-in [(:dbname m) :tables] (:tables m))
    :table (swap! dbs assoc-in [(:dbname m) :data (:table-name m)] (:data m))
    ;; :finished (win/static-alert (select-keys m [:missing :latency]))
    (win/growl {:message [:stress-test-error m] :delay :sticky :status :error})))

