package com.mkharytonau.rating

class NameMatcherSpec extends munit.FunSuite {
	import NameMatcher._

	test("exact match in cyrillic") {
		assert(NameMatcher("Харитонов Никита", "Харитонов Никита"))
	}

	test("exact match in latin") {
		assert(NameMatcher("Nikita Haritonov", "Nikita Haritonov"))
	}

	test("cyrillic to latin match") {
		assert(NameMatcher("Никита Харитонов", "Nikita Haritonov"))
	}

	test("latin to cyrillic match") {
		assert(NameMatcher("Nikita Haritonov", "Никита Харитонов"))
	}

	test("swapped cyrillic to latin match") {
		assert(NameMatcher("Харитонов Никита", "Nikita Haritonov"))
	}

	test("swapped latin to cyrillic match") {
		assert(NameMatcher("Nikita Haritonov", "Харитонов Никита"))
	}

	test("real case of custom aliases 1") {
		assert(NameMatcher("Куприянов Никита", "Kupriyanov Nikita"))
		assert(NameMatcher("Куприянов Никита", "NIKITA KUPRIYANOV"))
		assert(NameMatcher("Куприянов Никита", "KUPRIYANOV NIKITA"))
		assert(NameMatcher("Куприянов Никита", "KUPRYIANAU MIKITA"))
		assert(NameMatcher("KUPRIYANOV NIKITA", "KUPRYIANAU MIKITA"))
	}

	test("real case of custom aliases 2") {
		assert(NameMatcher("Шинкарёв Алексей", "Шинкарев Алексей"))
		assert(NameMatcher("Шинкарёв Алексей", "АЛЕКСЕЙ ШИНКАРЕВ"))
		assert(NameMatcher("Шинкарев Алексей", "АЛЕКСЕЙ ШИНКАРЕВ"))
	}

	test("real case of custom aliases 3") {
		assert(NameMatcher("Куделко Екатерина", "Куделко Катерина"))
	}

	test("non match") {
		assert(!NameMatcher("Иванов Иван", "Nikita Haritonov"))
	}
}