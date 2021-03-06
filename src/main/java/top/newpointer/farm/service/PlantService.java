package top.newpointer.farm.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.newpointer.farm.Signleton.PlantSet;
import top.newpointer.farm.mapper.PlantMapper;
import top.newpointer.farm.mapper.SpeciesMapper;
import top.newpointer.farm.pojo.Farmer;
import top.newpointer.farm.pojo.InteractLog;
import top.newpointer.farm.pojo.Land;
import top.newpointer.farm.pojo.Species;
import top.newpointer.farm.state.GrowState;
import top.newpointer.farm.state.Plant;
import top.newpointer.farm.state.PlantState;
import top.newpointer.farm.utils.StatusCode;

import java.util.*;

@Service
public class PlantService {

    @Autowired
    private SpeciesMapper speciesMapper;

    @Autowired
    private PlantMapper plantMapper;

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private LandService landService;

    @Autowired
    private BackpackSeedService backpackSeedService;

    @Autowired
    private SpeciesService speciesService;

    @Autowired
    private InteractLogService interactLogService;

    @Autowired
    private BackpackFruitService backpackFruitService;

    @Value("${maxLandNumber}")
    private Integer maxLandNumber;

    @Value("${yellowLandRate}")
    private Integer yellowLandRate;
    @Value("${redLandRate}")
    private Integer redLandRate;
    @Value("${blackLandRate}")
    private Integer blackLandRate;

    @Value("${accMoney}")
    private Integer accMoney;
    @Value("${accRate}")
    private Double accRate;
    @Value("${accTime}")
    private Integer accTime;

    @Value("${stealRate}")
    private Double stealRate;

    public void addPlant(Integer farmerId, Integer landId, Integer speciesId) {
        double restTime = speciesMapper.selectById(speciesId).getGrowthTime();
//        Plant plant = new Plant(null, farmerId, landId, speciesId, new Date(), restTime, 1, new GrowState());
        Plant plant = new Plant();
        plant.setFarmerId(farmerId);
        plant.setLandId(landId);
        plant.setSpeciesId(speciesId);
        plant.setSowingTime(new Date());
        plant.setRestTime(restTime);
        plant.setGrowthRate(0);
        plant.setPlantState(new GrowState());
        PlantSet.getInstance().addPlant(plant);
        plantMapper.insert(plant);
    }

    /**
     * @param farmerId
     * @param landId
     * @param speciesId
     * @return ??????????????????
     */
    public Integer sowSeed(Integer farmerId, Integer landId, Integer speciesId) {
        Integer seedNumber = backpackSeedService.getOneSeedNumber(farmerId, speciesId);
        //????????????????????????
        if (seedNumber < 1) {
            return StatusCode.NUMBER_NOT_ENOUGH;
        }
        //????????????????????????
        Plant plant = getPlantByFarmerIdAndLandId(farmerId, landId);
        if (plant != null) {
            return StatusCode.LAND_OCCUPIED;
        }
        //?????????????????????
        addPlant(farmerId, landId, speciesId);
        //????????????????????????
        backpackSeedService.alterSeeds(farmerId, speciesId, -1);
        return StatusCode.SUCCEED;
    }

    public Plant[] getPlantsByFarmerId(int farmerId) {
        List<Plant> plantList = PlantSet.getInstance().getPlantsByFarmerId(farmerId);

        Plant[] plantArray = new Plant[maxLandNumber];
        for (Plant plant : plantList) {
            Integer landId = plant.getLandId();
            plantArray[landId] = plant;
        }

        //????????????????????????
        for (Plant plant : plantArray) {
            if (plant != null) {
                Integer speciesId = plant.getSpeciesId();
                Species species = speciesService.getSpeciesById(speciesId);
                plant.setSpecies(species);
            }
        }
        return plantArray;
    }

    public void removePlantByFarmerIdAndLandId(int farmerId, int landId) {
        Plant selected = PlantSet.getInstance().getPlantByFarmerIdAndLandId(farmerId, landId);
        selected.dig();
    }

    public Plant getPlantByFarmerIdAndLandId(Integer farmerId, Integer landId) {
        QueryWrapper<Plant> wrapper = new QueryWrapper<>();
        wrapper.eq("farmer_id",farmerId)
                .eq("land_id",landId);
        Plant plant = plantMapper.selectOne(wrapper);
        return plant;
    }

    public void grow(Plant plant) {
        double landRate;
        Integer landId = plant.getLandId();
        Integer farmerId = plant.getFarmerId();
        Land land = landService.getLandByFarmerIdAndLandId(farmerId, landId);
        Integer landType = land.getType();
        if (Objects.equals(landType, Land.TYPE_YELLOW)) {
            landRate = yellowLandRate;
        } else if (Objects.equals(landType, Land.TYPE_RED)) {
            landRate = redLandRate;
        } else if (Objects.equals(landType, Land.TYPE_BLACK)) {
            landRate = blackLandRate;
        } else {
            System.err.println("???????????????????????????");
            landRate = 0;
        }
        double after = plant.getRestTime() - plant.getGrowthRate() - landRate;
        plant.setRestTime(after > 0 ? after : 0);
    }

    /**
     * ??????
     *
     * @param plant
     * @return ???????????????
     */
    public void harvest(Plant plant, Integer farmerId) {
        //??????????????????
        Farmer farmer = farmerService.getFarmerById(farmerId);
        //?????????????????????
        Integer fruitNumber = plant.getFruitNumber();
        backpackFruitService.alterFruit(farmerId,plant.getSpeciesId(),plant.getFruitNumber());
        //???????????????
        plant.dig();
        //???????????????
        Integer experience = farmer.getExperience();
        Integer getExperience = speciesService.getSpeciesById(plant.getSpeciesId()).getExperience();
        farmerService.setExperience(farmerId, experience + getExperience);
    }

    /**
     *
     * @param plant ???????????????
     * @param farmerId ????????????id
     * @return
     */
    public Integer steal(Plant plant, Integer farmerId) {
        //????????????????????????
        Integer beforeNumber = plant.getFruitNumber();
        //????????????????????????
        Integer stealNumber = (int) Math.floor(beforeNumber * stealRate);
        //?????????????????????
        backpackFruitService.alterFruit(farmerId,plant.getSpeciesId(),stealNumber);
        //???????????????
        plant.setFruitNumber(beforeNumber - stealNumber);
        //?????????????????????
        if(stealNumber != 0) {
            interactLogService.recordSteal(farmerId, plant.getFarmerId(), plant.getSpeciesId(), stealNumber);
        }
        return stealNumber;
    }

    public void setTimeToDeath(Plant plant) {
        Species species = speciesService.getSpeciesById(plant.getSpeciesId());
        double deadTime = species.getDeadTime();
        plant.setTimeToDeath(deadTime);
    }

    public Boolean buyAcceleration(Integer farmerId, Plant plant) {
        Double money = farmerService.getMoney(farmerId);
        if (money < accMoney) {//????????????
            return false;
        }
        //??????
        farmerService.setMoney(farmerId, money - accMoney);
        return true;
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     */
    @Async//???????????????endAccelerate???sleep?????????????????????????????????????????????
    public void accelerate(Plant plant) {
        plant.startAccelerate(plant, accRate);
        plant.endAccelerate(plant, accRate, accTime);
    }

    public void dying(Plant plant) {
        plant.setTimeToDeath(plant.getTimeToDeath() - 1);
    }
}
