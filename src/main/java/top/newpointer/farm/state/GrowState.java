package top.newpointer.farm.state;

import lombok.SneakyThrows;
import top.newpointer.farm.service.PlantService;
import top.newpointer.farm.GetBeanUtil;
import top.newpointer.farm.service.RedisService;
import top.newpointer.farm.service.SpeciesService;

public class GrowState extends PlantState{

    public static final Integer CODE = 0;

    private SpeciesService speciesService = GetBeanUtil.getBean(SpeciesService.class);;

    private Double dryProbability = Double.valueOf(GetBeanUtil.getPropertiesValue("dryProbability"));

//  此类未注入Spring容器，则也不能使用 @Autowired获取容器中内容
    private PlantService plantService = GetBeanUtil.getBean(PlantService.class);

    private RedisService redisService = GetBeanUtil.getBean(RedisService.class);

    @Override
    public Integer getCODE() {
        return CODE;
    }

    @Override
    public void updateTime() {
        //生长
        plantService.grow(super.plant);
        if(super.plant.getRestTime() == 0) {//剩余成熟时间为 0
            //更新状态
            super.plant.setPlantState(new RipeState());
            //更新果实数
            Integer speciesId = plant.getSpeciesId();
            Integer fruitNumber = speciesService.getSpeciesById(speciesId).getFruitNumber();
            super.plant.setFruitNumber(fruitNumber);
        }
        Double p;
        if(redisService.get("dryProbability") == null) {
            p = dryProbability;
        } else {
            p = (Double) redisService.get("dryProbability");
        }
        //随机缺水
        if(Math.random() < p ) {
            super.plant.setPlantState(new DryState());
            plantService.setTimeToDeath(plant);
        }
    }

    @Override
    public String water() {
        return "植物正在健康生长，无需浇水！";
    }

    @Override
    public String harvest(Integer farmerId) {
        return "植物正在健康生长，还未到收获期！";
    }

    @Override
    public Integer steal(Integer farmerId) {
        return null;
    }


    @Override
    public void startAccelerate(Plant plant, Double delta) {
        plant.setGrowthRate(plant.getGrowthRate() + delta);
    }

    @SneakyThrows
    @Override
    public void endAccelerate(Plant plant, Double delta,Integer time) {
        Thread.sleep(time * 1000);
        plant.setGrowthRate(plant.getGrowthRate() - delta);
    }
}
