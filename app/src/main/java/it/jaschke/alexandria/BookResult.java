package it.jaschke.alexandria;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by bungbagong on 5/10/2015.
 */
public class BookResult implements Parcelable {

    private String title;
    private String subtitle;
    private String desc;
    private String imgUrl;
    private ArrayList<String> categories;
    private ArrayList<String> authors;


    public BookResult(String title, String subtitle, String desc, String imgUrl, ArrayList<String> categories, ArrayList<String> authors) {
        this.title = title;
        this.subtitle = subtitle;
        this.desc = desc;
        this.imgUrl = imgUrl;
        this.categories = categories;
        this.authors = authors;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(desc);
        dest.writeString(imgUrl);
        dest.writeStringList(authors);
        dest.writeStringList(categories);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<BookResult> CREATOR
            = new Parcelable.Creator<BookResult>() {
        public BookResult createFromParcel(Parcel in) {
            return new BookResult(in);
        }

        public BookResult[] newArray(int size) {
            return new BookResult[size];
        }
    };

    private BookResult(Parcel in) {
        title = in.readString();
        subtitle = in.readString();
        desc = in.readString();
        imgUrl = in.readString();
        authors = in.readArrayList(null);
        categories = in.readArrayList(null);
    }


    public ArrayList<String> getAuthors() {
        return authors;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDesc() {
        return desc;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }
}
