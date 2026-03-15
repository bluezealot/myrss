package com.rssmonitor;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.Objects;

public class Article {
    private String title;
    private String chineseTitle;
    private String link;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant publishTime;
    
    private String source;
    private String description;
    private double score;

    public Article() {
    }

    public Article(String title, String link, Instant publishTime, String source, String description) {
        this.title = title;
        this.chineseTitle = null;
        this.link = link;
        this.publishTime = publishTime;
        this.source = source;
        this.description = description;
        this.score = 0.0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChineseTitle() {
        return chineseTitle;
    }

    public void setChineseTitle(String chineseTitle) {
        this.chineseTitle = chineseTitle;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Instant getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Instant publishTime) {
        this.publishTime = publishTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(link, article.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }

    @Override
    public String toString() {
        return "Article{" +
                "title='" + title + '\'' +
                ", chineseTitle='" + chineseTitle + '\'' +
                ", link='" + link + '\'' +
                ", publishTime=" + publishTime +
                ", source='" + source + '\'' +
                ", score=" + score +
                '}';
    }
}
