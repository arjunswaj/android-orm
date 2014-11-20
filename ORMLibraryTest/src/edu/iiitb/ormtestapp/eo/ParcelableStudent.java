package edu.iiitb.ormtestapp.eo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(name = "PARCELABLE_STUDENT")
public class ParcelableStudent implements Parcelable {

	@Id
	@Column(name = "_id")
	private long id;
	@Column(name = "AGE")
	private int age;

	@Transient
	private Date lastModifiedDate;

	public ParcelableStudent() {		
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public ParcelableStudent(int age) {
		this.age = age;
		lastModifiedDate = new Date();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(age);
	}

	@Transient
	public static final Parcelable.Creator<ParcelableStudent> CREATOR = new Parcelable.Creator<ParcelableStudent>() {
		public ParcelableStudent createFromParcel(Parcel in) {
			return new ParcelableStudent(in);
		}

		public ParcelableStudent[] newArray(int size) {
			return new ParcelableStudent[size];
		}
	};

	private ParcelableStudent(Parcel in) {
		age = in.readInt();
	}

}
