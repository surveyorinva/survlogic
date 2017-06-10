package com.survlogic.survlogic.model;

/**
 * Created by chrisfillmore on 5/26/2017.
 */

public class Satellite {
    private String mCardUseTypeId;
    private int mPrnNo, mConstellationType;
    private float mSnrCn0, mSvElevation, mSvAzimuth;
    private boolean mHasEphemeris, mHasAlmanac, mUsedinFix;

    public Satellite(String mCardUseTypeId, int mPrnNo, int mConstellationType, float mSnrCn0, float mSvElevation, float mSvAzimuth, boolean mHasEphemeris, boolean mHasAlmanac, boolean mUsedinFix) {
        this.mCardUseTypeId = mCardUseTypeId;
        this.mPrnNo = mPrnNo;
        this.mConstellationType = mConstellationType;
        this.mSnrCn0 = mSnrCn0;
        this.mSvElevation = mSvElevation;
        this.mSvAzimuth = mSvAzimuth;
        this.mHasEphemeris = mHasEphemeris;
        this.mHasAlmanac = mHasAlmanac;
        this.mUsedinFix = mUsedinFix;
    }

    public String getmCardUseTypeId() {
        return mCardUseTypeId;
    }

    public void setmCardUseTypeId(String mCardUseTypeId) {
        this.mCardUseTypeId = mCardUseTypeId;
    }

    public int getmPrnNo() {
        return mPrnNo;
    }

    public void setmPrnNo(int mPrnNo) {
        this.mPrnNo = mPrnNo;
    }

    public int getmConstellationType() {
        return mConstellationType;
    }

    public void setmConstellationType(int mConstellationType) {
        this.mConstellationType = mConstellationType;
    }

    public float getmSnrCn0() {
        return mSnrCn0;
    }

    public void setmSnrCn0(float mSnrCn0) {
        this.mSnrCn0 = mSnrCn0;
    }

    public float getmSvElevation() {
        return mSvElevation;
    }

    public void setmSvElevation(float mSvElevation) {
        this.mSvElevation = mSvElevation;
    }

    public float getmSvAzimuth() {
        return mSvAzimuth;
    }

    public void setmSvAzimuth(float mSvAzimuth) {
        this.mSvAzimuth = mSvAzimuth;
    }

    public boolean ismHasEphemeris() {
        return mHasEphemeris;
    }

    public void setmHasEphemeris(boolean mHasEphemeris) {
        this.mHasEphemeris = mHasEphemeris;
    }

    public boolean ismHasAlmanac() {
        return mHasAlmanac;
    }

    public void setmHasAlmanac(boolean mHasAlmanac) {
        this.mHasAlmanac = mHasAlmanac;
    }

    public boolean ismUsedinFix() {
        return mUsedinFix;
    }

    public void setmUsedinFix(boolean mUsedinFix) {
        this.mUsedinFix = mUsedinFix;
    }


}
