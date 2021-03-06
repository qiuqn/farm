package top.newpointer.farm.timer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.newpointer.farm.Signleton.PlantSet;
import top.newpointer.farm.state.Plant;

import java.util.Date;

@Component
public class RefreshTask {

    @Value("${dryProbability}")
    private Double waterProbability;

    private PlantSet observers = PlantSet.getInstance();

    @Scheduled(cron = "* * * * * *")//每秒执行一次
    public void refresh() {
        System.out.println(new Date());
        //更新时间
        observers.updateTime();
        //更新数据库
        observers.updatePlantsIntoDatabase();
    }
}
