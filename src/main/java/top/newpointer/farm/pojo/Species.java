package top.newpointer.farm.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Species {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private double seedPrice;
    private Integer unlockLevel;
    private double profit;
    private Integer experience;
    private double growthTime;
    //缺水后死亡倒计时时间
    private double deadTime;
    private Integer fruitNumber;
    private String imageFilename;
}
