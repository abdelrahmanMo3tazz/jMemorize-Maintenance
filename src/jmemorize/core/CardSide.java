package jmemorize.core;

import java.util.LinkedList;
import java.util.List;

/**
 * A card is made up of two card sides which can contain various contents, the
 * most important being text.
 * 
 * @author djemili
 */
public class CardSide implements Cloneable
{
    public interface CardSideObserver
    {
        public void onTextChanged(CardSide cardSide, FormattedText text);
        public void onImagesChanged(CardSide cardSide, List<String> imageIDs);
    }
    
    private FormattedText          mText;
    private List<String>           mImageIDs  = new LinkedList<String>();
    private List<CardSideObserver> mObservers = new LinkedList<CardSideObserver>();
    
    public CardSide()
    {
    }
    
    public CardSide(FormattedText text)
    {
        setText(text);
    }
    
    public FormattedText getText()
    {
        return mText;
    }
    
    /**
     * Note that using this method won't modify the modification date of the
     * card. Use {@link Card#setSides(String, String)} instead for modifications 
     * done by the user.
     */
    public void setText(FormattedText text)
    {
        if (text.equals(mText))
            return;
        
        mText = text;
        
        for (CardSideObserver observer : mObservers)
        {
            observer.onTextChanged(this, mText);
        }
    }
    
    /**
     * @return the IDs of all images of this card side.
     */
    public List<String> getImages()
    {
        return mImageIDs;
    }
    
    public void setImages(List<String> ids)
    {
        if (imageListsEqual(mImageIDs, ids))
            return;
        
        mImageIDs.clear();
        mImageIDs.addAll(ids);
        
        for (CardSideObserver observer : mObservers)
        {
            observer.onImagesChanged(this, mImageIDs);
        }
    }
    
    public void addObserver(CardSideObserver observer)
    {
        mObservers.add(observer);
    }
    
    public void removeObserver(CardSideObserver observer)
    {
        mObservers.remove(observer);
    }
    
    /** 
     * @return the unformatted string representation of the formatted text.
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return mText.getUnformatted();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        CardSide cardSide = new CardSide();
        cardSide.mText = (FormattedText)mText.clone();
        cardSide.mImageIDs.addAll(mImageIDs);
        
        return cardSide;
    }
    
    /**
     * Helper method to compare the contents of two lists, ignoring object references.
     * 
     * @param list1 The first list.
     * @param list2 The second list.
     * @return True if the lists contain the same elements, false otherwise.
     */
    private boolean imageListsEqual(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        return list1.containsAll(list2);
    }
}
