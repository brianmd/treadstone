(ns murphydye.fixed-data-table
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            ;; [jayq.core :refer [$ css html]]
            ;; [reagent.session :as session]
            ;; [re-com.core           :refer [h-box v-box box selection-list label title checkbox p line hyperlink-href]]
            ;; [re-com.selection-list :refer [selection-list-args-desc]]

            ;; [secretary.core :as secretary :include-macros true]
            ;; [goog.events :as events]
            ;; [goog.history.EventType :as HistoryEventType]
            ;; [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            ;; [re-com.core :as recom :refer [title p input-text input-textarea button selection-list scroller]]
            ;; [re-com.util     :refer [deref-or-value px]]
            ;; [re-com.popover  :refer [popover-tooltip]]
            ;; [re-com.box      :refer [h-box v-box box gap line flex-child-style align-style]]
            ;; [re-com.validate :refer [input-status-type? input-status-types-list regex?
            ;;                          string-or-hiccup? css-style? html-attr? number-or-string?
            ;;                          string-or-atom? throbber-size? throbber-sizes-list] :refer-macros [validate-args-macro]]

            ;; [cljsjs.react-data-grid :as  grid]
            [cljsjs.fixed-data-table]

            [murphydye.utils.core :as utils :refer [ppc]]
            ;; [murphydye.websockets :as ws]
            [murphydye.window :as win]
            ))

(def Table (r/adapt-react-class js/FixedDataTable.Table))
(def ColumnGroup (r/adapt-react-class js/FixedDataTable.ColumnGroup))
(def Column (r/adapt-react-class js/FixedDataTable.Column))
(def Cell (r/adapt-react-class js/FixedDataTable.Cell))

(defn gen-table
  "Generate `size` rows vector of 4 columns vectors to mock up the table."
  [size]
  (mapv (fn [i] [i                                                   ; Number
                 (rand-int 1000)                                     ; Amount
                 (rand)                                              ; Coeff
                 (rand-nth ["Here" "There" "Nowhere" "Somewhere"])]) ; Store
        (range 1 (inc size))))

;;; using custom :cellDataGetter in column for cljs persistent data structure
;;; is more efficient than converting row to js array in table's :rowGetter
(defn getter [k row] (get row k))

(defn table-component []
  (let [table  (gen-table 10)]
    (ppc table)
    [:div
     [Table {:width        600
             :height       400
             :rowHeight    30
             ;; :rowGetter    #(get table %)
             :rowsCount    (count table)
             :groupHeaderHeight 50
             :headerHeight 50}
      [ColumnGroup {:fixed true
                    :header [Cell "Col Group"]
                    ;; :width 400
                    }
       [Column {:fixed false
                :header [Cell "Col 1"]
                ;; :cell "<Cell>Column 1 static content</Cell>"
                :cell [Cell "Column 1 static content"]
                :height 14
                :width 200
                :flexGrow 2
                }]
       [Column {:fixed false
                :header [Cell "Col 2"]
                ;; :cell (fn [& args] (println args) [Cell (str args)])
                :cell (fn [args]
                        (let [{:keys [columnKey height width rowIndex] :as arg-map} (js->clj args :keywordize-keys true)]
                          (println arg-map)
                          [Cell (str "Row " rowIndex) "."]))
                ;; :cell (fn [{:keys [:columnKey :height :width :rowIndex]}] [Cell rowIndex])
                ;; :cell (fn [{:keys [:columnKey :height :width :rowIndex]}] [Cell rowIndex])
                :isResizable true
                :height 14
                :width 100
                :flexGrow 1
                }]
       ]
      ;; [Column {:label "Number" :dataKey 0 :cellDataGetter getter :width 100}]
      ;; [Column {:label "Amount" :dataKey 1 :cellDataGetter getter :width 100}]
      ;; [Column {:label "Coeff" :dataKey 2 :cellDataGetter getter :width 100}]
      ;; [Column {:label "Store" :dataKey 3 :cellDataGetter getter :width 100}]
      ]]))
;; header={<Cell>Col 1</Cell>}
;; cell={<Cell>Column 1 static content</Cell>}
;; width={2000}

