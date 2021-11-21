# farm

### 状态常量定义

#### 登陆相关：
public static final int PASSWORD_ERROR = -2;  
public static final int USER_NOT_EXIST = -1;  
public static final int SUCCEED = 1;  
public static final int NO_LOGIN = -3; 

#### 植物：
public static final Integer STATE_GROW = 0;  
public static final Integer STATE_DEATH = -1;  
public static final Integer STATE_WATER = -2;  
public static final Integer STATE_RIPE = 1;  

#### 土地：
public static final Integer TYPE_LOCKED = 0;  
public static final Integer TYPE_YELLOW = 1;  
public static final Integer TYPE_RED = 2;  
public static final Integer TYPE_BLACK = 3;  
public static final Integer TYPE_DRY = -1;  