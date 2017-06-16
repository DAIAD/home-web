package eu.daiad.web.service.etl.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.daiad.web.model.amphiro.AmphiroAbstractSession;
import eu.daiad.web.model.amphiro.AmphiroSession;


/**
 * Helper class for managing amphiro b1 sessions.
 */
public class MemorySessionStore {

    List<AmphiroAbstractSession> sessions;

    public MemorySessionStore(List<AmphiroAbstractSession> sessions) {
        this.sessions = sessions;

        Collections.sort(sessions, new Comparator<AmphiroAbstractSession>() {

            @Override
            public int compare(AmphiroAbstractSession s1, AmphiroAbstractSession s2) {
                AmphiroSession as1 = (AmphiroSession) s1;
                AmphiroSession as2 = (AmphiroSession) s2;

                if (as1.getId() == as2.getId()) {
                    throw new RuntimeException(String.format("Found duplicate session index [%d]",  as1.getId()));
                } else if (as1.getId() < as2.getId()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list.
     */
    public AmphiroSession get(int index) {
        return (AmphiroSession) sessions.get(index);
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list.
     */
    public int size() {
        return sessions.size();
    }

    /**
     * Returns true if this list contains no elements.
     *
     * @return true if this list contains no elements.
     */
    public boolean isEmpty() {
        return sessions.isEmpty();
    }

    /**
     * Removes all the historical data before the initial device pairing.
     *
     * @throws RuntimeException if a duplicate session index is found.
     */
    public void cleanPairingSessions() throws RuntimeException {

        int count = 0;

        for(AmphiroAbstractSession session : sessions) {
            AmphiroSession amphiroSession = (AmphiroSession) session;

            if(amphiroSession.isHistory()) {
                count++;
            } else {
                break;
            }
        }

        for (int i = 0; i < count; i++) {
            // Do not remove sessions
            //sessions.remove(0);
        }
    }

    /**
     * Given a timestamp, it returns the most recent real-time session before it.
     *
     * @param timestamp the timestamp to search.
     * @return the amphiro b1 real-time session that occurred before the given timestamp.
     */
    public AmphiroSession getRealTimeSessionBefore(long timestamp) {
        AmphiroSession result = null;

        for(AmphiroAbstractSession session : sessions) {
            AmphiroSession amphiroSession = (AmphiroSession) session;

            if ((!amphiroSession.isHistory()) && (amphiroSession.getTimestamp() <= timestamp)) {
                if ((result == null) || (result.getTimestamp() < amphiroSession.getTimestamp())) {
                    result = amphiroSession;
                }
            }
        }

        return result;
    }

    /**
     * Given a timestamp, it returns the most recent real-time session after it.
     *
     * @param timestamp the timestamp to search.
     * @return the amphiro b1 real-time session that occurred after the given timestamp.
     */
    public AmphiroSession getRealTimeSessionAfter(long timestamp) {
        AmphiroSession result = null;

        for(AmphiroAbstractSession session : sessions) {
            AmphiroSession amphiroSession = (AmphiroSession) session;

            if ((!amphiroSession.isHistory()) && (amphiroSession.getTimestamp() >= timestamp)) {
                if ((result == null) || (result.getTimestamp() > amphiroSession.getTimestamp())) {
                    result = amphiroSession;
                }
            }
        }

        return result;
    }


    /**
     * Given a timestamp, it returns the most recent real-time session before or after it.
     *
     * @param timestamp the timestamp to search.
     * @return the amphiro b1 real-time session that occurred most recently before or after the given timestamp.
     */
    public AmphiroSession getNearestSession(long timestamp) {
        AmphiroSession result = null;

        for(AmphiroAbstractSession session : sessions) {
            AmphiroSession amphiroSession = (AmphiroSession) session;

            if (!amphiroSession.isHistory()) {
                if ((result == null) ||
                    (Math.abs(result.getTimestamp() - timestamp) >
                     Math.abs(amphiroSession.getTimestamp() - timestamp))) {
                    result = amphiroSession;
                }
            }
        }

        return result;
    }

    /**
     * Returns the previous session id for the given id.
     *
     * @param id the session id to search for.
     * @return the id of the session which is immediately before the session with the given id.
     */
    public Long getBefore(long id) {
        for (int i = 0, count = sessions.size() - 1; i < count; i++) {
            if (((AmphiroSession) sessions.get(i + 1)).getId() == id) {
                return ((AmphiroSession) sessions.get(i)).getId();
            }
        }

        return null;
    }

    /**
     * Returns the next session id for the given id.
     *
     * @param id the session id to search for.
     * @return the id of the session which is immediately after the session with the given id.
     */
    public Long getNext(long id) {
        for (int i = 0, count = sessions.size() - 1; i < count; i++) {
            if (((AmphiroSession) sessions.get(i)).getId() == id) {
                return ((AmphiroSession) sessions.get(i + 1)).getId();
            }
        }

        return null;
    }

    /**
     * Returns the next session for the given id.
     *
     * @param id the session id to search for.
     * @return the session which is immediately after the session with the given id.
     */
    public AmphiroSession getNextSession(long id) {
        for (int i = 0, count = sessions.size() - 1; i < count; i++) {
            if (((AmphiroSession) sessions.get(i)).getId() == id) {
                return ((AmphiroSession) sessions.get(i + 1));
            }
        }

        return null;
    }

    /**
     * Given a timestamp, the two most recent real time sessions before and
     * after it are computed. The session index interval defined by the two
     * sessions is separated into two sets based on either (a) the given
     * weight values or (b) the number of household members and the number
     * of showers per week in the specific household. In the latter case,
     * the number of amphiro b1 devices is also considered.
     *
     * @param timestamp reference timestamp
     * @param interval1 weight for the first set.
     * @param interval2 weight of the second set.
     * @param householdMembers the number of household members.
     * @param showerPerWeek the number of showers per week.
     * @param numberOfAmphiro the number of installed amphiro devices.
     * @return the indexes of sessions that split the sessions between the
     *         given indexes in two sets based on the given weights.
     */
    public long[] interpolate(long timestamp, float interval1, float interval2, int householdMembers, Integer showerPerWeek, int numberOfAmphiro) {
        if(numberOfAmphiro > 3) {
            numberOfAmphiro = 3;
        }
        if (showerPerWeek == null) {
            showerPerWeek = 0;
        }

        AmphiroSession sessionBefore = getRealTimeSessionBefore(timestamp);
        AmphiroSession sessionAfter = getRealTimeSessionAfter(timestamp);

        if ((sessionBefore == null) || (sessionAfter == null)) {
            return null;
        }

        long[] result = new long[] { sessionBefore.getId(), sessionAfter.getId()};

        int count = 0;

        // Count sessions between the two indexes
        for(AmphiroAbstractSession session : sessions) {
            AmphiroSession amphiroSession = (AmphiroSession) session;
            if ((amphiroSession.getId() > sessionBefore.getId()) && (amphiroSession.getId() < sessionAfter.getId())) {
                count++;
            }
        }

        if (count == 0) {
            // No sessions found between the two indexes.
            return result;
        }

        // Check the result can be computed based on the household members
        // and number of showers per week
        int middle = 0;

        float threshold = ((float) showerPerWeek / 7) * 20 / numberOfAmphiro;
        if (count <= threshold) {
            middle = Math.round((count * (timestamp - sessionBefore.getTimestamp())) /
                                (sessionAfter.getTimestamp() - sessionBefore.getTimestamp()));
        } else {
            // Compute the middle index based on the weight values.
            middle = Math.round(count * interval1 / (interval1 + interval2));
        }

        count = 0;

        for (AmphiroAbstractSession session : sessions) {
            AmphiroSession amphiroSession = (AmphiroSession) session;
            if ((amphiroSession.getId() > sessionBefore.getId()) && (amphiroSession.getId() < sessionAfter.getId())) {
                if (count < middle) {
                    count++;
                    result[0] = amphiroSession.getId();
                } else {
                    result[1] = amphiroSession.getId();
                    return result;
                }
            }
        }

        return result;
    }
}
