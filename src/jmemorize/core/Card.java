/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2008 Riad Djemili and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version. 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package jmemorize.core;

import java.util.Date;
import java.util.List;

import jmemorize.core.CardSide.CardSideObserver;

/**
 * A flash card that has a front/flip side and can be learned.
 *
 * @author djemili
 * @version $Id: Card.java 1048 2008-01-21 21:40:00Z djemili $
 */
public class Card implements Events, Cloneable
{
    public static final long    ONE_DAY     = 1000 * 60 * 60 * 24;
    public static final boolean CLONE_DATES = Main.isDevel();

    // Changed these variable names to match convention
    private Category mCategory;
    private int mLevel;
    private CardSide mFrontSide = new CardSide();
    private CardSide mBackSide = new CardSide();
    private Date mDateTested;
    private Date mDateExpired;
    private Date mDateCreated;
    private Date mDateModified;
    private Date mDateTouched; // this date is used internally to order cards
    private int mTestsTotal;
    private int mTestsHit;    // successful learn repetitions
    private int mFrontHitsCorrect;
    private int mBackHitsCorrect;

    /**
     * Assumes formatted front- and backsides
     */
    public Card(String front, String back)
    {
        this(FormattedText.formatted(front), FormattedText.formatted(back));
    }

    public Card(FormattedText front, FormattedText back)
    {
        this(new Date(), front, back);
    }

    /**
     * The card sides are given in a formatted state.
     */
    public Card(Date created, String front, String back)
    {
        this(created, FormattedText.formatted(front), FormattedText.formatted(back));
    }

    public Card(Date created, FormattedText front, FormattedText back)
    {
        this(created, new CardSide(front), new CardSide(back));
    }

    public Card(Date created, CardSide frontSide, CardSide backSide)
    {
        mDateCreated = cloneDate(created);
        mDateModified = cloneDate(created);
        mDateTouched = cloneDate(created);
        mFrontSide = frontSide;
        mBackSide = backSide;
        attachCardSideObservers();
    }

    public void setSides(String front, String back)
    {
        FormattedText frontSide = FormattedText.unformatted(front);
        FormattedText backSide = FormattedText.unformatted(back);
        setSides(frontSide, backSide);
    }

    public void setSides(FormattedText front, FormattedText back)
            throws IllegalArgumentException
    {
        if (front.equals(mFrontSide.getText()) &&
                back.equals(mBackSide.getText()))
        {
            return;
        }
        mFrontSide.setText(front);
        mBackSide.setText(back);
        if (mCategory != null)
        {
            mDateModified = new Date();
            mCategory.fireCardEvent(EDITED_EVENT, this, getCategory(), mLevel);
        }
    }

    public int getLearnedAmount(boolean frontside)
    {
        return frontside ? mFrontHitsCorrect : mBackHitsCorrect;
    }

    public void setLearnedAmount(boolean frontside, int amount)
    {
        if (frontside)
        {
            mFrontHitsCorrect = amount;
        }
        else
        {
            mBackHitsCorrect = amount;
        }
        if (mCategory != null)
        {
            mCategory.fireCardEvent(DECK_EVENT, this, getCategory(), mLevel);
        }
    }

    public void incrementLearnedAmount(boolean frontside)
    {
        setLearnedAmount(frontside, getLearnedAmount(frontside) + 1);
    }

    public void resetLearnedAmount()
    {
        setLearnedAmount(true, 0);
        setLearnedAmount(false, 0);
    }

    public CardSide getFrontSide()
    {
        return mFrontSide;
    }

    public CardSide getBackSide()
    {
        return mBackSide;
    }

    public Date getDateTested()
    {
        return cloneDate(mDateTested);
    }

    public void setDateTested(Date date)
    {
        mDateTested = cloneDate(date);
        mDateTouched = cloneDate(date);
    }

    public Date getDateExpired()
    {
        return cloneDate(mDateExpired);
    }

    public void setDateExpired(Date date)
    {
        mDateExpired = cloneDate(date);
    }

    public Date getDateCreated()
    {
        return cloneDate(mDateCreated);
    }

    public void setDateCreated(Date date)
    {
        if (date == null)
            throw new NullPointerException();

        mDateCreated = cloneDate(date);
    }

    public Date getDateModified()
    {
        return mDateModified;
    }

    public void setDateModified(Date date)
    {
        if (date.before(mDateCreated))
            throw new IllegalArgumentException(
                    "Modification date can't be before creation date.");

        mDateModified = date;
    }

    public Date getDateTouched()
    {
        return cloneDate(mDateTouched);
    }

    public void setDateTouched(Date date)
    {
        mDateTouched = cloneDate(date);
    }

    public int getTestsTotal()
    {
        return mTestsTotal;
    }

    public int getTestsPassed()
    {
        return mTestsHit;
    }

    public int getPassRatio()
    {
        return (int)Math.round(100.0 * mTestsHit / mTestsTotal);
    }

    public void incStats(int hit, int total)
    {
        mTestsTotal += total;
        mTestsHit += hit;
    }

    public void resetStats()
    {
        mTestsTotal = 0;
        mTestsHit = 0;
        mFrontHitsCorrect = 0;
        mBackHitsCorrect = 0;
    }

    public Category getCategory()
    {
        return mCategory;
    }

    protected void setCategory(Category category)
    {
        mCategory = category;
    }

    public boolean isExpired()
    {
        Date now = Main.getNow();
        return mDateExpired != null &&
                (mDateExpired.before(now) || mDateExpired.equals(now));
    }

    public boolean isLearned()
    {
        return mDateExpired != null && mDateExpired.after(Main.getNow());
    }

    public boolean isUnlearned()
    {
        return mDateExpired == null;
    }

    public int getLevel()
    {
        return mLevel;
    }

    protected void setLevel(int level)
    {
        mLevel = level;
    }

    public Object clone()
    {
        Card card = null;
        try
        {
            card = (Card)super.clone();
            card.mFrontSide = (CardSide) mFrontSide.clone();
            card.mBackSide = (CardSide) mBackSide.clone();
            card.mDateCreated = cloneDate(mDateCreated);
            card.mDateExpired = cloneDate(mDateExpired);
            card.mDateModified = cloneDate(mDateModified);
            card.mDateTested = cloneDate(mDateTested);
            card.mDateTouched = cloneDate(mDateTouched);
            card.mCategory = null; // don't clone category
        }
        catch (CloneNotSupportedException e)
        {
            assert false;
        }
        return card;
    }

    public Card cloneWithoutProgress()
    {
        try
        {
            return new Card(mDateCreated,
                    (CardSide) mFrontSide.clone(), (CardSide) mBackSide.clone());
        }
        catch (CloneNotSupportedException e)
        {
            assert false;
            return null; // satisfy compiler
        }
    }

    public String toString()
    {
        return "("+ mFrontSide +"/"+ mBackSide +")";
    }

    private void attachCardSideObservers()
    {
        CardSideObserver observer = new CardSideObserver() {
            public void onImagesChanged(CardSide cardSide, List<String> imageIDs)
            {
                if (mCategory != null)
                {
                    mDateModified = new Date();
                    mCategory.fireCardEvent(EDITED_EVENT, Card.this, getCategory(), mLevel);
                }
            }

            public void onTextChanged(CardSide cardSide, FormattedText text)
            {
                // already handled by set sides
                // TODO handle event notifying here
            }
        };

        mFrontSide.addObserver(observer);
        mBackSide.addObserver(observer);
    }

    private Date cloneDate(Date date)
    {
        if (CLONE_DATES)
        {
            return date == null ? null : (Date)date.clone();
        }
        return date;
    }
}
