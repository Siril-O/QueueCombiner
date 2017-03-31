package com.ua.combiner.infrastructure;

public class Product {
    private String value;
    private String queueName;

    public Product(String value, String queueName) {
        this.value = value;
        this.queueName = queueName;
    }

    public String getValue() {
        return value;
    }

    public String getQueueName() {
        return queueName;
    }

    @Override
    public String toString() {
        return "Product{" +
                "value='" + value + '\'' +
                ", queueName='" + queueName + '\'' +
                '}';
    }
}
