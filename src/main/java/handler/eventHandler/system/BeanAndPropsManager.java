package handler.eventHandler.system;

import config.Constants;
import pojo.data.system.User;
import util.database.DatabaseUtil;

import java.util.*;

public class BeanAndPropsManager {

    public enum PropType {
        DOUBLE_EARN(0), HALF_COST(1);

        final int value;

        PropType(int i) {
            value = i;
        }
    }

    private static BeanAndPropsManager instance;

    private final HashSet<User> userBeanDaily;

    private final List<HashSet<User>> allGameProps;

    private BeanAndPropsManager() {
        userBeanDaily = new HashSet<>();
        allGameProps = new ArrayList<>();
        for (int i = 0; i < Constants.DATABASE_COLUMNS - 4; i++) {
            allGameProps.add(new HashSet<>());
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (HashSet<User> props: allGameProps) {
                    props.clear();
                }
            }
        }, calendar.getTime(), 24 * 3600 * 1000);
    }

    public static synchronized BeanAndPropsManager getInstance() {
        if (instance == null) {
            instance = new BeanAndPropsManager();
        }
        return instance;
    }

    public boolean useProps(User user, PropType type) {
        synchronized (allGameProps) {
            if (allGameProps.get(type.value).contains(user) ||
                    (type == PropType.DOUBLE_EARN && !DatabaseUtil.getInstance().useDoubleEarning(user.getId())) ||
                    (type == PropType.HALF_COST && !DatabaseUtil.getInstance().useHalfCost(user.getId()))) {
                return false;
            } else {
                allGameProps.get(type.value).add(user);
                return true;
            }
        }
    }

    public boolean buyProps(User user, PropType type, int num) {
        int totalSpend = num * Constants.PROP_PRICE;
        if (DatabaseUtil.getInstance().getBeanNum(user.getId()) < totalSpend) {
            return false;
        } else {
            if (type == PropType.DOUBLE_EARN) {
                DatabaseUtil.getInstance().updateDoubleEarning(user.getId(), num);
            } else {
                DatabaseUtil.getInstance().updateHalfCost(user.getId(), num);
            }
        }
        return true;
    }

    public boolean getBeanDaily(User user) {
        synchronized (userBeanDaily) {
            if (userBeanDaily.contains(user)) {
                return false;
            } else {
                DatabaseUtil.getInstance().updateBeanNum(user.getId(), Constants.DAILY_BEAN);
                userBeanDaily.add(user);
                return true;
            }
        }
    }

    public void addBeans(User user, int num) {
        if (num < 0) {
            return;
        }
        synchronized (allGameProps) {
            if (allGameProps.get(0).contains(user)) {
                num *= 2;
            }
        }
        DatabaseUtil.getInstance().updateBeanNum(user.getId(), num);
    }

    public void spendBeans(User user, int num) {
        if (num < 0) {
            return;
        }
        synchronized (allGameProps) {
            if (allGameProps.get(1).contains(user)) {
                num *= 2;
            }
        }
        DatabaseUtil.getInstance().updateBeanNum(user.getId(), -num);
    }


}
