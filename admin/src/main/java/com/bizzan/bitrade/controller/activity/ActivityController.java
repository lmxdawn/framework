package com.bizzan.bitrade.controller.activity;

import static org.springframework.util.Assert.notNull;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import com.bizzan.bitrade.entity.*;
import com.bizzan.bitrade.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.bizzan.bitrade.annotation.AccessLog;
import com.bizzan.bitrade.constant.AdminModule;
import com.bizzan.bitrade.constant.BooleanEnum;
import com.bizzan.bitrade.constant.PageModel;
import com.bizzan.bitrade.constant.SysConstant;
import com.bizzan.bitrade.constant.TransactionType;
import com.bizzan.bitrade.controller.common.BaseAdminController;
import com.bizzan.bitrade.util.DateUtil;
import com.bizzan.bitrade.util.MessageResult;
import com.bizzan.bitrade.vendor.provider.SMSProvider;
import com.sparkframework.security.Encrypt;

@RestController
@RequestMapping("activity/activity")
public class ActivityController extends BaseAdminController {
	@Autowired
	private ActivityService activityService;
	
	@Autowired
	private ActivityOrderService activityOrderService;
	
	@Autowired
	private MemberWalletService memberWalletService;
	
	@Autowired
	private MemberService memberService;
	
	@Autowired
    private LocaleMessageSourceService messageSource;

	@Autowired
	private MiningOrderService miningOrderService;

	@Autowired
	private LockedOrderService lockedOrderService;

    @Autowired
    private MemberTransactionService memberTransactionService;

	@Autowired
	private CoinService coinService;
	
    @Autowired
    private SMSProvider smsProvider;
    
	@Value("${spark.system.md5.key}")
    private String md5Key;
	
	/**
	 * ????????????
	 * @param pageModel
	 * @return
	 */
	@RequiresPermissions("activity:activity:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.ACTIVITY, operation = "????????????????????????Activity")
    public MessageResult activityList(PageModel pageModel) {
		if (pageModel.getProperty() == null) {
            List<String> list = new ArrayList<>();
            list.add("createTime");
            List<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setProperty(list);
            pageModel.setDirection(directions);
        }
        Page<Activity> all = activityService.findAll(null, pageModel.getPageable());
        return success(all);
    }
	@RequiresPermissions("activity:activity:locked-activity")
	@PostMapping("locked-activity")
	@AccessLog(module = AdminModule.ACTIVITY, operation = "????????????????????????")
	public MessageResult lockedActivityList() {
		List<Activity> all = activityService.findByTypeAndStep(6, 1); // ???????????????????????????????????????
		return success(all);
	}

	/**
	 * ??????????????????
	 * @param activity
	 * @return
	 */
	@RequiresPermissions("activity:activity:add")
    @PostMapping("add")
    @AccessLog(module = AdminModule.ACTIVITY, operation = "??????????????????Activity")
    public MessageResult ExchangeCoinList(
            @Valid Activity activity) {
		activity.setCreateTime(DateUtil.getCurrentDate());
		activity = activityService.save(activity);
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), activity);
    }
	
	/**
	 * ????????????????????????
	 * @param id
	 * @param progress
	 * @return
	 */
	@RequiresPermissions("activity:activity:modify-progress")
    @PostMapping("modify-progress")
    @AccessLog(module = AdminModule.ACTIVITY, operation = "??????????????????Activity")
    public MessageResult alterActivity(
            @RequestParam("id") Long id,
            @RequestParam("progress") Integer progress) {
		notNull(id, "validate id!");
		
		Activity result = activityService.findOne(id);
		notNull(result, "validate activity!");
		
		if(result.getProgress() > progress.intValue()) {
			return error("?????????????????????????????????");
		}
		result.setProgress(progress);
		
		activityService.save(result);
		
		return success(messageSource.getMessage("SUCCESS"));
	}
	
	/**
	 * ???????????????????????????
	 * @param id
	 * @param freezeAmount
	 * @return
	 */
	@RequiresPermissions("activity:activity:modify-freezeamount")
    @PostMapping("modify-freezeamount")
    @AccessLog(module = AdminModule.ACTIVITY, operation = "???????????????????????????Activity")
    public MessageResult alterActivityFreezeAmount(
            @RequestParam("id") Long id,
            @RequestParam("freezeAmount") BigDecimal freezeAmount) {
		notNull(id, "validate id!");
		
		Activity result = activityService.findOne(id);
		notNull(result, "validate activity!");
		
		if(result.getFreezeAmount().compareTo(freezeAmount) > 0) {
			return error("???????????????????????????????????????");
		}
		result.setFreezeAmount(freezeAmount);
		
		activityService.save(result);
		
		return success(messageSource.getMessage("SUCCESS"));
	}
	
	/**
	 * ????????????????????????
	 * @param id
	 * @param tradedAmount
	 * @return
	 */
	@RequiresPermissions("activity:activity:modify-tradedamount")
    @PostMapping("modify-tradedamount")
    @AccessLog(module = AdminModule.ACTIVITY, operation = "????????????????????????Activity")
    public MessageResult alterActivityTradedAmount(
            @RequestParam("id") Long id,
            @RequestParam("tradedAmount") BigDecimal tradedAmount) {
		notNull(id, "validate id!");
		
		Activity result = activityService.findOne(id);
		notNull(result, "validate activity!");
		
		if(result.getTradedAmount().compareTo(tradedAmount) > 0) {
			return error("???????????????????????????????????????");
		}
		result.setTradedAmount(tradedAmount);
		
		activityService.save(result);
		
		return success(messageSource.getMessage("SUCCESS"));
	}
	
	//Modify
	@RequiresPermissions("activity:activity:modify")
    @PostMapping("modify")
    @AccessLog(module = AdminModule.ACTIVITY, operation = "??????????????????Activity")
    public MessageResult alterActivity(
            @RequestParam("id") Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "detail", required = false) String detail,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "step", required = false) Integer step,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "totalSupply", required = false) BigDecimal totalSupply,
            @RequestParam(value = "price", required = false) BigDecimal price,
            @RequestParam(value = "priceScale", required = false) Integer priceScale,
            @RequestParam(value = "unit", required = false) String unit,
            @RequestParam(value = "acceptUnit", required = false) String acceptUnit,
            @RequestParam(value = "amountScale", required = false) Integer amountScale,
            @RequestParam(value = "maxLimitAmout", required = false) BigDecimal maxLimitAmout,
            @RequestParam(value = "minLimitAmout", required = false) BigDecimal minLimitAmout,
            @RequestParam(value = "limitTimes", required = false) Integer limitTimes,
            @RequestParam(value = "settings", required = false) String settings,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "smallImageUrl", required = false) String smallImageUrl,
            @RequestParam(value = "bannerImageUrl", required = false) String bannerImageUrl,
            @RequestParam(value = "noticeLink", required = false) String noticeLink,
            @RequestParam(value = "activityLink", required = false) String activityLink,
            @RequestParam(value = "leveloneCount", required = false) Integer leveloneCount,
            @RequestParam(value = "holdLimit", required = false) BigDecimal holdLimit,
            @RequestParam(value = "holdUnit", required = false) String holdUnit,
            @RequestParam(value = "miningDays", required = false) Integer miningDays,
            @RequestParam(value = "miningDaysprofit", required = false) BigDecimal miningDaysprofit,
            @RequestParam(value = "miningUnit", required = false) String miningUnit,
            @RequestParam(value = "miningInvite", required = false) BigDecimal miningInvite,
            @RequestParam(value = "miningInvitelimit", required = false) BigDecimal miningInvitelimit,
            @RequestParam(value = "miningPeriod", required = false) Integer miningPeriod,
			@RequestParam(value = "lockedUnit", required = false) String lockedUnit,
			@RequestParam(value = "lockedPeriod", required = false) Integer lockedPeriod,
			@RequestParam(value = "lockedDays", required = false) Integer lockedDays,
			@RequestParam(value = "releaseType", required = false) Integer releaseType,
			@RequestParam(value = "releasePercent", required = false) BigDecimal releasePercent,
			@RequestParam(value = "lockedFee", required = false) BigDecimal lockedFee,
			@RequestParam(value = "releaseAmount", required = false) BigDecimal releaseAmount,
			@RequestParam(value = "releaseTimes", required = false) BigDecimal releaseTimes,

            @RequestParam(value = "password") String password,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
		password = Encrypt.MD5(password + md5Key);
        Assert.isTrue(password.equals(admin.getPassword()), messageSource.getMessage("WRONG_PASSWORD"));
        
        Activity result = activityService.findOne(id);
        
        notNull(result, "validate activity!");
        
        if(title != null) result.setTitle(title);
        if(detail != null) result.setDetail(detail);
        if(status != null) result.setStatus(status == 0 ? BooleanEnum.IS_FALSE : BooleanEnum.IS_TRUE);
        if(step != null) result.setStep(step);
        if(type != null) result.setType(type);
        if(startTime != null) result.setStartTime(startTime);
        if(endTime != null) result.setEndTime(endTime);
        if(totalSupply != null) result.setTotalSupply(totalSupply);
        if(price != null) result.setPrice(price);
        if(priceScale != null) result.setPriceScale(priceScale);
        if(unit != null) result.setUnit(unit);
        if(acceptUnit != null) result.setAcceptUnit(acceptUnit);
        if(amountScale != null) result.setAmountScale(amountScale);
        if(maxLimitAmout != null) result.setMaxLimitAmout(maxLimitAmout);
        if(minLimitAmout != null) result.setMinLimitAmout(minLimitAmout);
        if(limitTimes != null) result.setLimitTimes(limitTimes);
        if(settings != null) result.setSettings(settings);
        if(content != null) result.setContent(content);
        if(smallImageUrl != null) result.setSmallImageUrl(smallImageUrl);
        if(bannerImageUrl != null) result.setBannerImageUrl(bannerImageUrl);
        if(noticeLink != null) result.setNoticeLink(noticeLink);
        if(activityLink != null) result.setActivityLink(activityLink);
        if(leveloneCount != null) result.setLeveloneCount(leveloneCount);
        if(holdLimit != null) result.setHoldLimit(holdLimit);
        if(holdUnit != null) result.setHoldUnit(holdUnit);
        if(miningDays != null) result.setMiningDays(miningDays);
        if(miningDaysprofit != null) result.setMiningDaysprofit(miningDaysprofit);
        if(miningUnit != null) result.setMiningUnit(miningUnit);
        if(miningInvite != null) result.setMiningInvite(miningInvite);
        if(miningInvitelimit != null) result.setMiningInvitelimit(miningInvitelimit);
        if(miningPeriod != null) result.setMiningPeriod(miningPeriod);
		if(lockedUnit != null) result.setLockedUnit(lockedUnit);
		if(lockedPeriod != null) result.setLockedPeriod(lockedPeriod);
		if(lockedDays != null) result.setLockedDays(lockedDays);
		if(releaseType != null) result.setReleaseType(releaseType);
		if(releasePercent != null) result.setReleasePercent(releasePercent);
		if(lockedFee != null) result.setLockedFee(lockedFee);
		if(releaseAmount != null) result.setReleaseAmount(releaseAmount);
		if(releaseTimes != null) result.setReleaseTimes(releaseTimes);

        activityService.saveAndFlush(result);
        return success(messageSource.getMessage("SUCCESS"));
	}
	
	@RequiresPermissions("activity:activity:detail")
    @GetMapping("{id}/detail")
    public MessageResult detail(
            @PathVariable Long id) {
        Activity activity = activityService.findById(id);
        Assert.notNull(activity, "validate id!");
        return success(activity);
    }
	
	@RequiresPermissions("activity:activity:orderlist")
    @GetMapping("{aid}/orderlist")
    public MessageResult orderList(
            @PathVariable Long aid) {
        List<ActivityOrder> activityOrderList = activityOrderService.findAllByActivityId(aid);
        Assert.notNull(activityOrderList, "validate id!");
        return success(activityOrderList);
    }
	
	/**
	 * ???????????????
	 * @return
	 */
	@RequiresPermissions("activity:activity:distribute")
    @PostMapping("distribute")
    @AccessLog(module = AdminModule.ACTIVITY, operation = "???????????????Activity")
	@Transactional(rollbackFor = Exception.class)
	public MessageResult distribute(@RequestParam("oid") Long oid) {
		ActivityOrder order = activityOrderService.findById(oid);
		if(order == null) {
			return error("???????????????");
		}
		if(order.getState() != 1) {
			return error("?????????????????????????????????????????????");
		}
		Activity activity = activityService.findById(order.getActivityId());
		if(activity == null) {
			return error("???????????????");
		}
		// 1???2???3???4????????????????????????????????????????????????
		if(activity.getType() == 1 || activity.getType() == 2 || activity.getType() == 3 || activity.getType() == 4) {
			// ??????????????????
			if(activity.getStep() != 2) {
				return error("??????????????????");
			}
		}
		
		// type = 3??????????????????
		// ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		if(activity.getType() == 3) {
			// ??????????????????acceptUnit???
			MemberWallet freezeWallet = memberWalletService.findByCoinUnitAndMemberId(activity.getAcceptUnit(), order.getMemberId());
			if(freezeWallet == null) {
				return error("????????????????????????");
			}
			memberWalletService.thawBalance(freezeWallet, order.getFreezeAmount());
			
			// ??????????????????unit???(?????????????????????)
			MemberWallet distributeWallet = memberWalletService.findByCoinUnitAndMemberId(activity.getUnit(), order.getMemberId());

			if(distributeWallet == null) {
				return error("????????????????????????");
			}
			// ???????????? = ?????? / ????????? * ???????????????
			BigDecimal disAmount = order.getFreezeAmount().divide(activity.getFreezeAmount()).multiply(activity.getTotalSupply()).setScale(activity.getAmountScale(), BigDecimal.ROUND_HALF_DOWN);
			// ????????????????????????
			memberWalletService.increaseBalance(distributeWallet.getId(), disAmount);

	        MemberTransaction memberTransaction = new MemberTransaction();
	        memberTransaction.setFee(BigDecimal.ZERO);
	        memberTransaction.setAmount(disAmount);
	        memberTransaction.setMemberId(distributeWallet.getMemberId());
	        memberTransaction.setSymbol(activity.getUnit());
	        memberTransaction.setType(TransactionType.ACTIVITY_BUY);
	        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
	        memberTransaction.setRealFee("0");
	        memberTransaction.setDiscountFee("0");
	        memberTransaction= memberTransactionService.save(memberTransaction);
	        
	        Member member = memberService.findOne(order.getMemberId());
			try {
 				smsProvider.sendCustomMessage(member.getMobilePhone(), "??????????????????????????????????????????" + disAmount + activity.getUnit() + "????????????");
 			} catch (Exception e) {
 				return error(e.getMessage());
 			}
			
			// ??????????????????
			order.setState(2);// ?????????
			order.setAmount(disAmount); //????????????
			activityOrderService.saveAndFlush(order);
			
			return success("????????????????????????????????????????????????" + disAmount);
		}
		
		// type = 4??????????????????
		if(activity.getType() == 4) {
			// ?????????????????????
			MemberWallet freezeWallet = memberWalletService.findByCoinUnitAndMemberId(activity.getAcceptUnit(), order.getMemberId());
			if(freezeWallet == null) {
				return error("????????????????????????");
			}
			memberWalletService.decreaseFrozen(freezeWallet.getId(), order.getTurnover());
			
			MemberTransaction memberTransaction1 = new MemberTransaction();
			memberTransaction1.setFee(BigDecimal.ZERO);
			memberTransaction1.setAmount(order.getTurnover().negate());
			memberTransaction1.setMemberId(freezeWallet.getMemberId());
	        memberTransaction1.setSymbol(activity.getAcceptUnit());
	        memberTransaction1.setType(TransactionType.ACTIVITY_BUY);
	        memberTransaction1.setCreateTime(DateUtil.getCurrentDate());
	        memberTransaction1.setRealFee("0");
	        memberTransaction1.setDiscountFee("0");
	        memberTransaction1 = memberTransactionService.save(memberTransaction1);
	        
			// ???????????????
			BigDecimal disAmount = order.getAmount();
			MemberWallet distributeWallet = memberWalletService.findByCoinUnitAndMemberId(activity.getUnit(), order.getMemberId());
			if(distributeWallet == null) {
				return error("????????????????????????");
			}
			memberWalletService.increaseBalance(distributeWallet.getId(), disAmount);
			
			MemberTransaction memberTransaction = new MemberTransaction();
	        memberTransaction.setFee(BigDecimal.ZERO);
	        memberTransaction.setAmount(disAmount);
	        memberTransaction.setMemberId(distributeWallet.getMemberId());
	        memberTransaction.setSymbol(activity.getUnit());
	        memberTransaction.setType(TransactionType.ACTIVITY_BUY);
	        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
	        memberTransaction.setRealFee("0");
	        memberTransaction.setDiscountFee("0");
	        memberTransaction= memberTransactionService.save(memberTransaction);
	        
			// ??????????????????
			order.setState(2);//?????????
			activityOrderService.saveAndFlush(order);
			
			Member member = memberService.findOne(order.getMemberId());
			try {
 				smsProvider.sendCustomMessage(member.getMobilePhone(), "??????????????????????????????????????????" + disAmount + activity.getUnit() + "????????????");
 			} catch (Exception e) {
 				return error(e.getMessage());
 			}
			
			return success("????????????????????????????????????????????????" + disAmount);
		}
		
		// ?????????????????????
		if(activity.getType() == 5) {
			// ?????????????????????
			MemberWallet freezeWallet = memberWalletService.findByCoinUnitAndMemberId(activity.getAcceptUnit(), order.getMemberId());
			if(freezeWallet == null) {
				return error("????????????????????????");
			}
			memberWalletService.decreaseFrozen(freezeWallet.getId(), order.getTurnover());
			
			MemberTransaction memberTransaction1 = new MemberTransaction();
			memberTransaction1.setFee(BigDecimal.ZERO);
			memberTransaction1.setAmount(order.getTurnover().negate());
			memberTransaction1.setMemberId(freezeWallet.getMemberId());
	        memberTransaction1.setSymbol(activity.getAcceptUnit());
	        memberTransaction1.setType(TransactionType.ACTIVITY_BUY);
	        memberTransaction1.setCreateTime(DateUtil.getCurrentDate());
	        memberTransaction1.setRealFee("0");
	        memberTransaction1.setDiscountFee("0");
	        memberTransaction1 = memberTransactionService.save(memberTransaction1);
	        
	        // ??????????????????
 			order.setState(2);//?????????
 			activityOrderService.saveAndFlush(order);
 			
 			// ????????????
 			for(int i = 0; i < order.getAmount().intValue(); i++) {
	 			Date currentDate = DateUtil.getCurrentDate();
	 			MiningOrder mo = new MiningOrder();
	 			mo.setActivityId(activity.getId());
	 			mo.setMemberId(order.getMemberId());
	 			mo.setMiningDays(activity.getMiningDays());
	 			mo.setMiningDaysprofit(activity.getMiningDaysprofit());
	 			mo.setMiningUnit(activity.getMiningUnit());
	 			mo.setCurrentDaysprofit(activity.getMiningDaysprofit());
	 			mo.setCreateTime(currentDate);
	 			mo.setEndTime(DateUtil.dateAddDay(currentDate, activity.getMiningDays()));
	 			mo.setImage(activity.getSmallImageUrl());
	 			mo.setTitle(activity.getTitle());
	 			mo.setMiningStatus(1); //???????????????1???????????????
	 			mo.setMiningedDays(0); //?????????0???
	 			mo.setTotalProfit(BigDecimal.ZERO);
	 			mo.setType(0); // ????????????
	 			mo.setMiningInvite(activity.getMiningInvite()); // ??????
	 			mo.setMiningInvitelimit(activity.getMiningInvitelimit()); // ????????????
	 			mo.setPeriod(activity.getMiningPeriod()); // ??????????????????
	 			miningOrderService.save(mo);
 			}
 			Member member = memberService.findOne(order.getMemberId());
 			// ???????????????????????????(??????????????????????????????????????????????????????
 			if(activity.getMiningInvite().compareTo(BigDecimal.ZERO) > 0) {
 				if(member != null) {
 					if(member.getInviterId() != null) {
 						Member inviter = memberService.findOne(member.getInviterId());
 						List<MiningOrder> miningOrders = miningOrderService.findAllByMemberIdAndActivityId(inviter.getId(), activity.getId());
 						if(miningOrders.size() > 0) {
 							for(MiningOrder item : miningOrders) {
 								// ????????????????????????????????????
 								if(item.getCurrentDaysprofit().subtract(item.getMiningDaysprofit()).divide(item.getMiningDaysprofit()).compareTo(activity.getMiningInvitelimit()) < 0) {
 									// ???????????????
 									BigDecimal newMiningDaysprofit = item.getCurrentDaysprofit().add(item.getMiningDaysprofit().multiply(activity.getMiningInvite()));
 									// ?????????????????????????????????
 									if(newMiningDaysprofit.compareTo(item.getMiningDaysprofit().add(item.getMiningDaysprofit().multiply(activity.getMiningInvitelimit()))) > 0) {
 										newMiningDaysprofit = item.getMiningDaysprofit().add(item.getMiningDaysprofit().multiply(activity.getMiningInvitelimit()));
 									}
 									item.setCurrentDaysprofit(newMiningDaysprofit);
 									miningOrderService.save(item);
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 			
 			try {
 				smsProvider.sendCustomMessage(member.getMobilePhone(), "?????????????????????????????????????????????????????????");
 			} catch (Exception e) {
 				return error(e.getMessage());
			}
			return success("??????????????????");
		}

		// ?????????????????????
		if(activity.getType() == 6) {
			if(activity.getReleaseTimes().compareTo(BigDecimal.ZERO) <=0  || activity.getReleaseTimes() == null) {
				return error("?????????????????????0???");
			}
			// ?????????????????????
			MemberWallet freezeWallet = memberWalletService.findByCoinUnitAndMemberId(activity.getAcceptUnit(), order.getMemberId());
			if(freezeWallet == null) {
				return error("????????????????????????");
			}
			// ?????????????????????????????????
			memberWalletService.decreaseFrozen(freezeWallet.getId(), order.getTurnover());

			MemberTransaction memberTransaction1 = new MemberTransaction();
			memberTransaction1.setFee(BigDecimal.ZERO);
			memberTransaction1.setAmount(order.getTurnover().negate());
			memberTransaction1.setMemberId(freezeWallet.getMemberId());
			memberTransaction1.setSymbol(activity.getAcceptUnit());
			memberTransaction1.setType(TransactionType.ACTIVITY_BUY);
			memberTransaction1.setCreateTime(DateUtil.getCurrentDate());
			memberTransaction1.setRealFee("0");
			memberTransaction1.setDiscountFee("0");
			memberTransaction1 = memberTransactionService.save(memberTransaction1);

			// ToRelease ?????????????????????( = ?????????????????? * ???????????????
			memberWalletService.increaseToRelease(freezeWallet.getId(), order.getAmount().multiply(activity.getReleaseTimes()));

			// ??????????????????
			order.setState(2);//?????????
			activityOrderService.saveAndFlush(order);

			// ??????????????????
			Date currentDate = DateUtil.getCurrentDate();
			LockedOrder lo = new LockedOrder();
			lo.setActivityId(activity.getId());
			lo.setMemberId(order.getMemberId());
			lo.setLockedDays(activity.getLockedDays());
			lo.setReleasedDays(0);
			lo.setReleaseUnit(activity.getLockedUnit());
			lo.setReleaseType(activity.getReleaseType());
			lo.setPeriod(activity.getLockedPeriod());
			lo.setLockedStatus(1); // ????????????????????????
			lo.setReleasePercent(activity.getReleasePercent());
			lo.setReleaseCurrentpercent(activity.getReleasePercent());
			lo.setImage(activity.getSmallImageUrl());
			lo.setTitle(activity.getTitle());
			lo.setTotalLocked(order.getAmount().multiply(activity.getReleaseTimes()));
			lo.setReleaseTimes(activity.getReleaseTimes());
			lo.setOriginReleaseamount(order.getAmount().multiply(activity.getReleaseTimes()).divide(BigDecimal.valueOf(activity.getLockedDays()), 8, BigDecimal.ROUND_HALF_DOWN));
			lo.setCurrentReleaseamount(order.getAmount().multiply(activity.getReleaseTimes()).divide(BigDecimal.valueOf(activity.getLockedDays()), 8, BigDecimal.ROUND_HALF_DOWN));
			lo.setTotalRelease(BigDecimal.ZERO);
			lo.setLockedInvite(activity.getMiningInvite());
			lo.setLockedInvitelimit(activity.getMiningInvitelimit());

			// ????????????????????????
			if(activity.getLockedPeriod() == 0) lo.setEndTime(DateUtil.dateAddDay(currentDate, activity.getLockedDays()));
			// ????????????????????????
			if(activity.getLockedPeriod() == 1) lo.setEndTime(DateUtil.dateAddDay(currentDate, activity.getLockedDays() * 7));
			// ????????????????????????
			if(activity.getLockedPeriod() == 2) lo.setEndTime(DateUtil.dateAddMonth(currentDate, activity.getLockedDays()));
			// ????????????????????????
			if(activity.getLockedPeriod() == 3) lo.setEndTime(DateUtil.dateAddYear(currentDate, activity.getLockedDays()));

			lockedOrderService.save(lo);

			Member member = memberService.findOne(order.getMemberId());
			// ???????????????????????????(??????????????????????????????????????????????????????
			if(activity.getMiningInvite().compareTo(BigDecimal.ZERO) > 0) {
				if(member != null) {
					if(member.getInviterId() != null) {
						Member inviter = memberService.findOne(member.getInviterId());
						List<LockedOrder> lockedOrders = lockedOrderService.findAllByMemberIdAndActivityId(inviter.getId(), activity.getId());
						if(lockedOrders.size() > 0) {
							for(LockedOrder item : lockedOrders) {
								// ????????????????????????????????????
								if(item.getCurrentReleaseamount().subtract(item.getOriginReleaseamount()).divide(item.getOriginReleaseamount()).compareTo(activity.getMiningInvitelimit()) < 0) {
									// ???????????????
									BigDecimal newReleaseAmount = item.getCurrentReleaseamount().add(item.getCurrentReleaseamount().multiply(activity.getMiningInvite()));
									// ?????????????????????????????????
									if(newReleaseAmount.compareTo(item.getOriginReleaseamount().add(item.getOriginReleaseamount().multiply(activity.getMiningInvitelimit()))) > 0) {
										newReleaseAmount = item.getOriginReleaseamount().add(item.getOriginReleaseamount().multiply(activity.getMiningInvitelimit()));
									}
									item.setCurrentReleaseamount(newReleaseAmount);
									lockedOrderService.save(item);
									break;
								}
							}
						}
					}
				}
			}

			try {
				smsProvider.sendCustomMessage(member.getMobilePhone(), "????????????????????????????????????????????????????????????");
			} catch (Exception e) {
				return error(e.getMessage());
			}
			return success("????????????");
		}

		if(activity.getType() == 6) {

		}
		return error("??????????????????");
	}

	/**
	 * ?????????????????????????????????
	 * @param pageModel
	 * @return
	 */
	@RequiresPermissions("activity:activity:lock-member-coin")
	@PostMapping("lock-member-coin")
	@AccessLog(module = AdminModule.ACTIVITY, operation = "????????????????????????Activity")
	public MessageResult lockMemberCoin(@RequestParam("memberId") Long memberId,
										@RequestParam("activityId") Long activityId,
										@RequestParam("unit") String unit,
										@RequestParam("amount") BigDecimal amount) {
		// ????????????????????????
		Member member = memberService.findOne(memberId);
		org.springframework.util.Assert.notNull(member, "??????!");

		// ????????????????????????
		Activity activity = activityService.findOne(activityId);
		org.springframework.util.Assert.notNull(activity, "??????????????????!");
		// ??????????????????????????????
		if(activity.getType() != 6) {
			return MessageResult.error("???????????????????????????");
		}
		// ??????????????????????????????
		if(activity.getStep() != 1) {
			return MessageResult.error("??????????????????????????????");
		}

		// ???????????????/???????????????
		if(activity.getMinLimitAmout().compareTo(BigDecimal.ZERO) > 0) {
			if(activity.getMinLimitAmout().compareTo(amount) > 0) {
				return MessageResult.error("??????????????????????????????");
			}
		}
		if(activity.getMaxLimitAmout().compareTo(BigDecimal.ZERO) > 0 || activity.getLimitTimes() > 0) {
			// ???????????????/???????????????(????????????????????????)
			List<ActivityOrder> orderDetailList = activityOrderService.findAllByActivityIdAndMemberId(member.getId(), activityId);
			BigDecimal alreadyAttendAmount = BigDecimal.ZERO;
			int alreadyAttendTimes = 0;
			if(orderDetailList != null) {
				alreadyAttendTimes = orderDetailList.size();
				for(int i = 0; i < orderDetailList.size(); i++) {
					if(activity.getType() == 3) {
						alreadyAttendAmount = alreadyAttendAmount.add(orderDetailList.get(i).getFreezeAmount());
					}else {
						alreadyAttendAmount = alreadyAttendAmount.add(orderDetailList.get(i).getAmount());
					}
				}
			}
			// ???????????????
			if(activity.getMaxLimitAmout().compareTo(BigDecimal.ZERO) > 0) {
				if(alreadyAttendAmount.add(amount).compareTo(activity.getMaxLimitAmout()) > 0) {
					return MessageResult.error("??????????????????????????????");
				}
			}
			// ??????????????????
			if(activity.getLimitTimes() > 0) {
				if(activity.getLimitTimes() < alreadyAttendTimes + 1) {
					return MessageResult.error("?????????????????????");
				}
			}
		}

		// ??????????????????
		if(activity.getHoldLimit().compareTo(BigDecimal.ZERO) > 0 && activity.getHoldUnit() != null && activity.getHoldUnit() != "") {
			MemberWallet holdCoinWallet = memberWalletService.findByCoinUnitAndMemberId(activity.getHoldUnit(), member.getId());
			if (holdCoinWallet == null) {
				return MessageResult.error("??????????????????????????????");
			}
			if(holdCoinWallet.getIsLock().equals(BooleanEnum.IS_TRUE)){
				return MessageResult.error("??????????????????????????????");
			}
			if(holdCoinWallet.getBalance().compareTo(activity.getHoldLimit()) < 0) {
				return MessageResult.error("??????" + activity.getHoldUnit() +"??????????????????????????????");
			}
		}

		// ????????????????????????
		Coin coin;
		coin = coinService.findByUnit(activity.getAcceptUnit());
		if (coin == null) {
			return MessageResult.error("???????????????");
		}

		// ????????????????????????
		MemberWallet acceptCoinWallet = memberWalletService.findByCoinUnitAndMemberId(activity.getAcceptUnit(), member.getId());
		if (acceptCoinWallet == null || acceptCoinWallet == null) {
			return MessageResult.error("?????????????????????");
		}
		if(acceptCoinWallet.getIsLock().equals(BooleanEnum.IS_TRUE)){
			return MessageResult.error("???????????????");
		}

		// ????????????????????????
		BigDecimal totalAcceptCoinAmount = BigDecimal.ZERO;
		totalAcceptCoinAmount = amount.add(activity.getLockedFee()).setScale(activity.getAmountScale(), BigDecimal.ROUND_HALF_DOWN);

		if(acceptCoinWallet.getBalance().compareTo(totalAcceptCoinAmount) < 0){
			return MessageResult.error("??????????????????");
		}

		ActivityOrder activityOrder = new ActivityOrder();
		activityOrder.setActivityId(activityId);
		activityOrder.setAmount(amount); // ??????????????????
		activityOrder.setFreezeAmount(totalAcceptCoinAmount); // ??????????????????????????????????????????????????????????????????
		activityOrder.setBaseSymbol(activity.getAcceptUnit());
		activityOrder.setCoinSymbol(activity.getUnit());
		activityOrder.setCreateTime(DateUtil.getCurrentDate());
		activityOrder.setMemberId(member.getId());
		activityOrder.setPrice(activity.getPrice());
		activityOrder.setState(1); //?????????
		activityOrder.setTurnover(totalAcceptCoinAmount);//??????????????????????????????????????????????????????????????????????????????????????????
		activityOrder.setType(activity.getType());

		MessageResult mr = activityOrderService.saveActivityOrder(member.getId(), activityOrder);

		if (mr.getCode() != 0) {
			return MessageResult.error(500, "??????????????????:" + mr.getMessage());
		}else {
			return MessageResult.success("??????????????????????????????????????????????????????");
		}
	}
}
