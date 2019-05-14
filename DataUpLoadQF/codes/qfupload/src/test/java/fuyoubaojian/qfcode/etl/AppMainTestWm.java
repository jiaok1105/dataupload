package fuyoubaojian.qfcode.etl;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.xfire.client.Client;

/**
 * 启动容器
 */
public class AppMainTestWm {
	//webService URL
	private static final String WEB_URL = "https://124.227.108.50:6001/dhow/ep/soap/PremaritalService?wsdl";
	public static void main(String[] args) {			

		try{
			String jsonStr="\"PREMARITAL_CONSULT\":\"1\","
					+"\"IS_GESTATION\":\"2\","
					+"\"THALASSEMIA\":\"3\","
					+"\"IS_HIV\":\"4\","
					+"\"SYPHILIS\":\"5\","
					+"\"HEPATITIS\":\"6\","
					+"\"GONORRHOEA\":\"7\","
					+"\"TRICHOMONAD\":\"8\","
					+"\"MOULD\":\"9\","
					+"\"MEDICAL_OPINION\":\"10\","
					+"\"ANAMNESIS_HAVE\":\"11\","
					+"\"SEX\":\"12\","
					+"\"CONTAGION\":\"13\","
					+"\"CONTAGION_NUM\":\"14\","
					+"\"GENETIC\":\"15\","
					+"\"PSYCHOSIS_NUM\":\"16\","
					+"\"REPRODUCTIVE_DISEASES_NUM\":\"17\","
					+"\"REPRODUCTIVE_DISEASES_G\":\"18\","
					+"\"MEDICINE_DISEASES_NUM\":\"19\","
					+"\"ANAMNESIS\":\"20\","
					+"\"MARITAL_CONTENT\":\"21\","
					+"\"HEREDITY_CONTENT\":\"22\","
					+"\"NEAR_RELATION_CON\":\"23\","
					+"\"INTELLIGENCE_CONTENT\":\"24\","
					+"\"BREAST_CONTENT\":\"25\","
					+"\"SPECIAL_FACE\":\"26\","
					+"\"PREMARITAL_CONTENT\":\"27\","
					+"\"SHED_CONTENT\":\"28\","
					+"\"OTORHINOLARYNG_CONTENT\":\"29\","
					+"\"THYROID_CONTENT\":\"30\","
					+"\"RHYTHM_CON\":\"31\","
					+"\"LUNG_CONTENT\":\"32\","
					+"\"NOISE_CONTENT\":\"33\","
					+"\"LIMBS_SPINAL_CONTENT\":\"34\","
					+"\"OPERATION_CONTENT\":\"35\","
					+"\"ANUS_VULVA_CON\":\"36\","
					+"\"ANUS_SECRETA_CON\":\"37\","
					+"\"ANUS_UTERUS_CON\":\"38\","
					+"\"ANUS_ACCESSORY_CON\":\"39\","
					+"\"VAGINA_VULVA_CON\":\"40\","
					+"\"VAGINA_SECRETA_CON\":\"41\","
					+"\"VAGINA_CON\":\"42\","
					+"\"VAGINA_CERVIX_CON\":\"43\","
					+"\"VAGINA_UTERUS_CON\":\"44\","
					+"\"VAGINA_ACCESSORY_CON\":\"45\","
					+"\"EDUCATION\":\"46\","
					+"\"BLOODSHED\":\"47\","
					+"\"OPERATION_H\":\"48\","
					+"\"PRESENT_H\":\"49\","
					+"\"FLOW_VALUE\":\"50\","
					+"\"DYSMENORRHEA\":\"51\","
					+"\"MARITAL_H\":\"52\","
					+"\"HEREDITY_H\":\"53\","
					+"\"NEAR_RELATION\":\"54\","
					+"\"SPECIAL\":\"55\","
					+"\"PSYCHOSIS\":\"56\","
					+"\"INTELLIGENCE\":\"57\","
					+"\"SHED\":\"58\","
					+"\"OTORHINOLARYNG\":\"59\","
					+"\"THYROID\":\"60\","
					+"\"RHYTHM\":\"61\","
					+"\"NOISE\":\"62\","
					+"\"LUNG\":\"63\","
					+"\"LIVER\":\"64\","
					+"\"LIMBS_SPINAL\":\"65\","
					+"\"PUBES\":\"66\","
					+"\"BREAST\":\"67\","
					+"\"ANUS_VULVA\":\"68\","
					+"\"ANUS_SECRETA\":\"69\","
					+"\"ANUS_UTERUS\":\"70\","
					+"\"ANUS_ACCESSORY\":\"71\","
					+"\"VAGINA_VULVA\":\"72\","
					+"\"VAGINA\":\"73\","
					+"\"VAGINA_CERVIX\":\"74\","
					+"\"VAGINA_UTERUS\":\"75\","
					+"\"VAGINA_ACCESSORY\":\"76\","
					+"\"AGREE_CHECK\":\"77\","
					+"\"POMUM\":\"78\","
					+"\"PUBES_BOY\":\"79\","
					+"\"PENIS\":\"80\","
					+"\"WRAPPING\":\"81\","
					+"\"EPIDIDYMIS\":\"82\","
					+"\"VARICOCELE\":\"83\","
					+"\"VARICOCELE_POSITION\":\"84\","
					+"\"CHECK_RESULT\":\"85\","
					+"\"OCCUPATION\":\"86\","
					+"\"VAGINA_SECRETA\":\"87\","
					+"\"PREMARITAL_COHABIT\":\"88\","
					+"\"CONTRACEPTION\":\"89\","
					+"\"SEMEN_CHECK\":\"90\","
					+"\"INFERTILITY_PUZZLE\":\"91\","
					+"\"CONSULTATION\":\"92\","
					+"\"HIV_SCREEN\":\"93\",";
				System.out.println("ws输入：" + jsonStr);  
		   }catch(Exception e){
				e.printStackTrace();
		   }
		}

	public static Client getClient() {
		Client client=null;
		try{
			client=new Client(new URL(WEB_URL));
		}catch (MalformedURLException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return client;
	}
}
