package com.calo.cmpp.module;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class NumberTextField extends PlainDocument {

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }

        char[] s = str.toCharArray();
        int length = 0;

        // Filter out non-numeric characters
        for (int i = 0; i < s.length; i++) {
            if (Character.isDigit(s[i])) {
                s[length++] = s[i];
            }
        }

        // Insert the content
        super.insertString(offset, new String(s, 0, length), attr);
    }
}
