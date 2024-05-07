package jmemorize.core;

import java.util.Date;
import java.util.List;

import jmemorize.core.CardSide.CardSideObserver;

public class Card implements Events, Cloneable {
    public static final long ONE_DAY = 1000 * 60 * 60 * 24;
    public static final boolean CLONE_DATES = true; // Assuming date cloning is needed

    private Category mCategory;
    private int mLevel;
    private CardSide mFrontSide = new CardSide();
    private CardSide mBackSide = new CardSide();
    private Date mDateTested;
    private Date mDateExpired;
    private Date mDateCreated;
    private Date mDateModified;
    private Date mDateTouched;
    private int mTestsTotal;
    private int mTestsHit;
    private int mFrontHitsCorrect;
    private int mBackHitsCorrect;

    public Card(String front, String back) {
        this(FormattedText.formatted(front), FormattedText.formatted(back));
    }

    public Card(FormattedText front, FormattedText back) {
        this(new Date(), front, back);
    }

    public Card(Date created, String front, String back) {
        this(created, FormattedText.formatted(front), FormattedText.formatted(back));
    }

    public Card(Date created, FormattedText front, FormattedText back) {
        this(created, new CardSide(front), new CardSide(back));
    }

    public Card(Date created, CardSide frontSide, CardSide backSide) {
        mDateCreated = cloneDate(created);
        mDateModified = cloneDate(created);
        mDateTouched = cloneDate(created);
        mFrontSide = frontSide;
        mBackSide = backSide;
        attachCardSideObservers();
    }

    public void setSides(String front, String back) {
        FormattedText frontSide = FormattedText.unformatted(front);
        FormattedText backSide = FormattedText.unformatted(back);
        setSides(frontSide, backSide);
    }

    public void setSides(FormattedText front, FormattedText back) throws IllegalArgumentException {
        if (front.equals(mFrontSide.getText()) &&
                back.equals(mBackSide.getText())) {
            return;
        }
        mFrontSide.setText(front);
        mBackSide.setText(back);
        if (mCategory != null) {
            mDateModified = new Date();
            mCategory.fireCardEvent(EDITED_EVENT, this, getCategory(), mLevel);
        }
    }

    public int getLearnedAmount(boolean frontside) {
        return frontside ? mFrontHitsCorrect : mBackHitsCorrect;
    }

    public void setLearnedAmount(boolean frontside, int amount) {
        if (frontside) {
            mFrontHitsCorrect = amount;
        } else {
            mBackHitsCorrect = amount;
        }
        if (mCategory != null) {
            mCategory.fireCardEvent(DECK_EVENT, this, getCategory(), mLevel);
        }
    }

    // Other methods...

    private void attachCardSideObservers() {
        CardSideObserver observer = new CardSideObserver() {
            public void onImagesChanged(CardSide cardSide, List<String> imageIDs) {
                if (mCategory != null) {
                    mDateModified = new Date();
                    mCategory.fireCardEvent(EDITED_EVENT, Card.this, getCategory(), mLevel);
                }
            }

            public void onTextChanged(CardSide cardSide, FormattedText text) {
                // already handled by set sides
                // TODO handle event notifying here
            }
        };

        mFrontSide.addObserver(observer);
        mBackSide.addObserver(observer);
    }

    private Date cloneDate(Date date) {
        return date == null ? null : (Date) date.clone();
    }

    public void setDateModified(Date date) {
        if (date.before(mDateCreated) || date.equals(mDateCreated))
            throw new IllegalArgumentException(
                    "Modification date must be after or equal to the creation date.");

        mDateModified = cloneDate(date);
    }
}
