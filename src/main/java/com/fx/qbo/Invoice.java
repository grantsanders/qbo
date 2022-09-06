package com.fx.qbo;

import java.util.ArrayList;

public class Invoice {
    CustomerRef CustomerRef = new CustomerRef();
    ArrayList<Line> Line = new ArrayList<Line>();

    public Invoice() {
        Line.add(new Line());
    }

    class Line {
        public Line() {
        }

        private SalesItemLineDetail SalesItemLineDetail = new SalesItemLineDetail();
        private double Amount = 100;
        private String DetailType = "SalesItemLineDetail";
        private String Description = "bruh";

        class SalesItemLineDetail {
            public SalesItemLineDetail() {
            }

            private ItemRef ItemRef = new ItemRef();

            class ItemRef {
                private String name = "Services";
                private String value = "1";
            }
        }

    }

    class CustomerRef {
        public CustomerRef() {
        }

        private String value = "1";
        private String name = "";
    }

}
