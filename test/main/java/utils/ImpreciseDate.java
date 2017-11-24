package main.java.utils;

import java.util.Date;

/**
 * Impressive Date equality doesn't take into consideration milliseconds.
 * You should be using this one to compare database dates against json responses.
 */
public class ImpreciseDate {
    private Date date;

    public ImpreciseDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        ImpreciseDate date = (ImpreciseDate) o;
        return this.date.getTime() / 1000 == date.date.getTime() / 1000;
    }
}
