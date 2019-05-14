package fuyoubaojian.qfcode.utils;

/**
 * 系统常量
 */
public class Constants {
	/**
	 * 批量迁移时每次迁移量的大小
	 */
	public final static int qk_sum = 1;

	/**
	 * 已实现数据迁移的表，当前做了11个实现
	 */
	public final static String[] tableNames = new String[] {
			"WA_PUERPERA_BASIC_INFO", "WA_PUERPERA_REGISTER",
			"WA_PUERPERA_INQUIRY", "WA_PUERPERA_PHYSICAL", "WA_DELIVERY_INFO", "WA_DELIVERY_NEONATE",
			"WA_POSTPARTUM_INTERVIEW","WA_PUERPERA_HIGH_RISK", "CA_NEONATE_BIRTH", "CA_NEONATE_VISIT",
			"CA_PHYSICAL_EXAM" };
}
