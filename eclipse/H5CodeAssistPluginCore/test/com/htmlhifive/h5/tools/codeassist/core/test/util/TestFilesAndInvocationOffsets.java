package com.htmlhifive.h5.tools.codeassist.core.test.util;

public enum TestFilesAndInvocationOffsets {

	UNIT1(Const.PATH_UNIT1, 41), UNIT2(Const.PATH_UNIT2, 63), UNIT3(Const.PATH_UNIT3, 52), UNIT4(Const.PATH_UNIT4, 77), UNIT5(
			Const.PATH_UNIT5, 107), UNIT6(Const.PATH_UNIT6, 61), UNIT7(Const.PATH_UNIT7, 72), UNIT8(Const.PATH_UNIT8,
			116), UNIT9(Const.PATH_UNIT9, 86), UNIT10(Const.PATH_UNIT10, 92), UNIT11(Const.PATH_UNIT11, 87), UNIT12(
			Const.PATH_UNIT12, 162), SAMPLE_CONTROLLER(Const.PATH_SAMPLE_CONTROLLER, 402), UNIT13(Const.PATH_UNIT13,
			101), UNIT14(Const.PATH_UNIT14, 128), RETURNCHECK6(Const.PATH_RETURNCHECK6, 175), RETURNCHECK7(
			Const.PATH_RETURNCHECK7, 185), RETURNCHECK8(Const.PATH_RETURNCHECK8, 230), RETURNCHECK9(
			Const.PATH_RETURNCHECK9, 200), RETURNCHECK10(Const.PATH_RETURNCHECK10, 194), UNIT15(Const.PATH_UNIT15, 138), EVENTCONTEXT1(
			Const.PATH_EVENTCONTEXT1, 76), EVENTCONTEXT2(Const.PATH_EVENTCONTEXT2, 101);

	private final String unitPath;

	private final int invocationOffset;

	private TestFilesAndInvocationOffsets(String unitPath, int invocationOffset) {

		this.unitPath = unitPath;
		this.invocationOffset = invocationOffset;
	}

	private static class Const {
		public static final String PATH_TESTJS_DIRECTORY = "testPlugin/testjs/controllerTest/";
		public static final String PATH_UNIT1 = PATH_TESTJS_DIRECTORY + "unit01.js";
		public static final String PATH_UNIT2 = PATH_TESTJS_DIRECTORY + "unit02.js";
		public static final String PATH_UNIT3 = PATH_TESTJS_DIRECTORY + "unit03.js";
		public static final String PATH_UNIT4 = PATH_TESTJS_DIRECTORY + "unit04.js";
		public static final String PATH_UNIT5 = PATH_TESTJS_DIRECTORY + "unit05.js";
		public static final String PATH_UNIT6 = PATH_TESTJS_DIRECTORY + "unit06.js";
		public static final String PATH_UNIT7 = PATH_TESTJS_DIRECTORY + "unit07.js";
		public static final String PATH_UNIT8 = PATH_TESTJS_DIRECTORY + "unit08.js";
		public static final String PATH_UNIT9 = PATH_TESTJS_DIRECTORY + "unit09.js";
		public static final String PATH_UNIT10 = PATH_TESTJS_DIRECTORY + "unit10.js";
		public static final String PATH_UNIT11 = PATH_TESTJS_DIRECTORY + "unit11.js";
		public static final String PATH_UNIT12 = PATH_TESTJS_DIRECTORY + "unit12.js";
		public static final String PATH_SAMPLE_CONTROLLER = PATH_TESTJS_DIRECTORY + "sample_controller.js";
		public static final String PATH_UNIT13 = PATH_TESTJS_DIRECTORY + "unit13.js";
		public static final String PATH_UNIT14 = PATH_TESTJS_DIRECTORY + "unit14.js";
		public static final String PATH_UNIT15 = PATH_TESTJS_DIRECTORY + "unit15.js";
		public static final String PATH_RETURNCHECK6 = PATH_TESTJS_DIRECTORY + "returnCheck06.js";
		public static final String PATH_RETURNCHECK7 = PATH_TESTJS_DIRECTORY + "returnCheck07.js";
		public static final String PATH_RETURNCHECK8 = PATH_TESTJS_DIRECTORY + "returnCheck08.js";
		public static final String PATH_RETURNCHECK9 = PATH_TESTJS_DIRECTORY + "returnCheck09.js";
		public static final String PATH_RETURNCHECK10 = PATH_TESTJS_DIRECTORY + "returnCheck10.js";
		public static final String PATH_EVENTCONTEXT_DIRECTORY = "testPlugin/testjs/eventContext/";
		public static final String PATH_EVENTCONTEXT1 = PATH_EVENTCONTEXT_DIRECTORY + "eventcontext1.js";
		public static final String PATH_EVENTCONTEXT2 = PATH_EVENTCONTEXT_DIRECTORY + "eventcontext2.js";
	}

	/**
	 * unitPathを取得する.
	 * 
	 * @return unitPath
	 */
	public String getUnitPath() {

		return unitPath;
	}

	/**
	 * invocationOffsetを取得する.
	 * 
	 * @return invocationOffset
	 */
	public int getInvocationOffset() {

		return invocationOffset;
	}

}
