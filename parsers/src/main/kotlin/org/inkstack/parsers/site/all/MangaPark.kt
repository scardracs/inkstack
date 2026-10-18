package org.inkstack.parsers.site.all

import androidx.collection.ArrayMap
import kotlinx.coroutines.coroutineScope
import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.core.PagedMangaParser
import org.inkstack.parsers.model.*
import org.inkstack.parsers.util.*
import org.inkstack.parsers.util.suspendlazy.suspendLazy
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Broken
@MangaSourceParser("MANGAPARK", "MangaPark")
internal class MangaPark(context: MangaLoaderContext) :
	PagedMangaParser(context, MangaParserSource.MANGAPARK, pageSize = 36) {

	override val configKeyDomain = ConfigKey.Domain(
		"mangapark.net",
		"mangapark.com",
		"mangapark.org",
		"mangapark.me",
		"mangapark.io",
		"mangapark.to",
		"comicpark.org",
		"comicpark.to",
		"readpark.org",
		"readpark.net",
		"parkmanga.com",
		"parkmanga.net",
		"parkmanga.org",
		"mpark.to",
	)

	override fun onCreateConfig(keys: MutableCollection<ConfigKey<*>>) {
		super.onCreateConfig(keys)
		keys.add(userAgentKey)
	}

	override val availableSortOrders: Set<SortOrder> =
		EnumSet.of(SortOrder.POPULARITY, SortOrder.UPDATED, SortOrder.NEWEST, SortOrder.ALPHABETICAL, SortOrder.RATING)

	override val filterCapabilities: MangaListFilterCapabilities
		get() = MangaListFilterCapabilities(
			isMultipleTagsSupported = true,
			isTagsExclusionSupported = true,
			isSearchSupported = true,
			isSearchWithFiltersSupported = true,
			isOriginalLocaleSupported = true,
		)

	override suspend fun getFilterOptions() = MangaListFilterOptions(
		availableTags = tagsMap.get().values.toSet(),
		availableStates = EnumSet.of(
			MangaState.ONGOING,
			MangaState.FINISHED,
			MangaState.ABANDONED,
			MangaState.PAUSED,
			MangaState.UPCOMING,
		),
		availableContentRating = EnumSet.of(ContentRating.SAFE),
		availableLocales = setOf(
			Locale.forLanguageTag("af"), Locale.forLanguageTag("sq"), Locale.forLanguageTag("am"), Locale.forLanguageTag("ar"), Locale.forLanguageTag("hy"),
			Locale.forLanguageTag("az"), Locale.forLanguageTag("be"), Locale.forLanguageTag("bn"), Locale.forLanguageTag("zh-hk"), Locale.forLanguageTag("zh-tw"),
			Locale.CHINESE, Locale.forLanguageTag("ceb"), Locale.forLanguageTag("ca"), Locale.forLanguageTag("km"), Locale.forLanguageTag("my"),
			Locale.forLanguageTag("bg"), Locale.forLanguageTag("bs"), Locale.forLanguageTag("hr"), Locale.forLanguageTag("cs"), Locale.forLanguageTag("da"),
			Locale.forLanguageTag("nl"), Locale.ENGLISH, Locale.forLanguageTag("et"), Locale.forLanguageTag("fo"), Locale.forLanguageTag("fil"),
			Locale.forLanguageTag("fi"), Locale.forLanguageTag("he"), Locale.forLanguageTag("ha"), Locale.forLanguageTag("jv"), Locale.forLanguageTag("lb"),
			Locale.forLanguageTag("mn"), Locale.forLanguageTag("ro"), Locale.forLanguageTag("si"), Locale.forLanguageTag("ta"), Locale.forLanguageTag("uz"),
			Locale.forLanguageTag("ur"), Locale.forLanguageTag("tg"), Locale.forLanguageTag("sd"), Locale.forLanguageTag("pt-br"), Locale.forLanguageTag("mo"),
			Locale.forLanguageTag("lt"), Locale.JAPANESE, Locale.ITALIAN, Locale.forLanguageTag("ht"), Locale.forLanguageTag("lv"),
			Locale.forLanguageTag("mr"), Locale.forLanguageTag("pt"), Locale.forLanguageTag("sn"), Locale.forLanguageTag("sv"), Locale.forLanguageTag("uk"),
			Locale.forLanguageTag("tk"), Locale.forLanguageTag("sw"), Locale.forLanguageTag("st"), Locale.forLanguageTag("pl"), Locale.forLanguageTag("mi"),
			Locale.forLanguageTag("lo"), Locale.forLanguageTag("ga"), Locale.forLanguageTag("gu"), Locale.forLanguageTag("gn"), Locale.forLanguageTag("id"),
			Locale.forLanguageTag("ky"), Locale.forLanguageTag("mt"), Locale.forLanguageTag("fa"), Locale.forLanguageTag("sh"), Locale.forLanguageTag("es-419"),
			Locale.forLanguageTag("tr"), Locale.forLanguageTag("to"), Locale.forLanguageTag("vi"), Locale.forLanguageTag("es"), Locale.forLanguageTag("sr"),
			Locale.forLanguageTag("ps"), Locale.forLanguageTag("ml"), Locale.forLanguageTag("ku"), Locale.forLanguageTag("ig"), Locale.forLanguageTag("el"),
			Locale.GERMAN, Locale.forLanguageTag("is"), Locale.KOREAN, Locale.forLanguageTag("ms"), Locale.forLanguageTag("ny"), Locale.forLanguageTag("sm"),
			Locale.forLanguageTag("so"), Locale.forLanguageTag("ti"), Locale.forLanguageTag("zu"), Locale.forLanguageTag("yo"), Locale.forLanguageTag("th"),
			Locale.forLanguageTag("sl"), Locale.forLanguageTag("ru"), Locale.forLanguageTag("no"), Locale.forLanguageTag("mg"), Locale.forLanguageTag("kk"),
			Locale.forLanguageTag("hu"), Locale.forLanguageTag("ka"), Locale.FRENCH, Locale.forLanguageTag("hi"), Locale.forLanguageTag("kn"),
			Locale.forLanguageTag("mk"), Locale.forLanguageTag("ne"), Locale.forLanguageTag("rm"), Locale.forLanguageTag("sk"), Locale.forLanguageTag("te"),
		),
	)

	init {
		context.cookieJar.insertCookies(domain, "nsfw", "2")
	}

	override suspend fun getListPage(page: Int, order: SortOrder, filter: MangaListFilter): List<Manga> {
		val url = buildString {
			append("https://")
			append(domain)
			append("/search?page=")
			append(page.toString())
			filter.query?.let {
				append("&word=")
				append(filter.query.urlEncoded())
			}

			append("&genres=")
			filter.tags.joinTo(this, ",") { it.key }

			append("|")
			filter.tagsExclude.joinTo(this, ",") { it.key }

			if (filter.contentRating.isNotEmpty()) {
				filter.contentRating.oneOrThrowIfMany()?.let {
					append(
						when (it) {
							ContentRating.SAFE -> append(",gore,bloody,violence,ecchi,adult,mature,smut,hentai")
							else -> append("")
						},
					)
				}
			}

			filter.states.oneOrThrowIfMany()?.let {
				append("&status=")
				append(
					when (it) {
						MangaState.ONGOING -> "ongoing"
						MangaState.FINISHED -> "completed"
						MangaState.PAUSED -> "hiatus"
						MangaState.ABANDONED -> "cancelled"
						MangaState.UPCOMING -> "pending"
						else -> throw IllegalArgumentException("$it not supported")
					},
				)
			}

			append("&sortby=")
			append(
				when (order) {
					SortOrder.POPULARITY -> "views_d000"
					SortOrder.UPDATED -> "field_update"
					SortOrder.NEWEST -> "field_create"
					SortOrder.ALPHABETICAL -> "field_name"
					SortOrder.RATING -> "field_score"
					else -> ""

				},
			)

			filter.locale?.let {
				append("&lang=")
				append(it.language)
			}

			filter.originalLocale?.let {
				append("&orig=")
				append(it.language)
			}
		}

		val doc = webClient.httpGet(url).parseHtml()
		return doc.select("div.grid.gap-5 div.flex.border-b").map { div ->
			val href = div.selectFirstOrThrow("a").attrAsRelativeUrl("href")
			Manga(
				id = generateUid(href),
				url = href,
				publicUrl = href.toAbsoluteUrl(div.host ?: domain),
				coverUrl = div.selectFirst("img")?.src(),
				title = div.selectFirst("h3")?.text().orEmpty(),
				altTitles = emptySet(),
				rating = div.selectFirst("span.text-yellow-500")?.text()?.toFloatOrNull()?.div(10F) ?: RATING_UNKNOWN,
				tags = emptySet(),
				authors = emptySet(),
				state = null,
				source = source,
				contentRating = if (isNsfwSource) ContentRating.ADULT else null,
			)
		}
	}

	private val tagsMap = suspendLazy(initializer = ::parseTags)

	private suspend fun parseTags(): Map<String, MangaTag> {
		val tagElements = webClient.httpGet("https://$domain/search").parseHtml()
			.select("div.flex-col:contains(Genres) div.whitespace-nowrap")
		val tagMap = ArrayMap<String, MangaTag>(tagElements.size)
		for (el in tagElements) {
			val name = el.selectFirstOrThrow("span.whitespace-nowrap").text().toTitleCase(sourceLocale)
			if (name.isEmpty()) continue
			val key = el.attr("q:key")
			if (key.isEmpty()) continue
			tagMap[name] = MangaTag(
				title = name,
				key = key,
				source = source,
			)
		}
		return tagMap
	}

	override suspend fun getDetails(manga: Manga): Manga = coroutineScope {
		val doc = webClient.httpGet(manga.url.toAbsoluteUrl(domain)).parseHtml()
		val tagMap = tagsMap.get()
		val selectTag = doc.select("div[q:key=30_2] span.whitespace-nowrap")
		val tags = selectTag.mapNotNullToSet { tagMap[it.text()] }
		val nsfw = tags.any { t -> t.key == "hentai" || t.key == "adult" }
		val dateFormat = SimpleDateFormat("dd/MM/yyyy", sourceLocale)
		val author = doc.selectFirst("div[q:key=tz_4]")?.textOrNull()
		manga.copy(
			altTitles = setOfNotNull(doc.selectFirst("div[q:key=tz_2]")?.textOrNull()),
			authors = setOfNotNull(author),
			description = doc.selectFirst("react-island[q:key=0a_9]")?.html(),
			state = when (doc.selectFirst("span[q:key=Yn_5]")?.text()?.lowercase()) {
				"ongoing" -> MangaState.ONGOING
				"completed" -> MangaState.FINISHED
				"hiatus" -> MangaState.PAUSED
				"cancelled" -> MangaState.ABANDONED
				else -> null
			},
			tags = tags,
			contentRating = if (nsfw) ContentRating.ADULT else ContentRating.SAFE,
			chapters = doc.body().select("div.group.flex div.px-2").mapChapters(reversed = true) { i, div ->
				val a = div.selectFirstOrThrow("a")
				val href = a.attrAsRelativeUrl("href")
				val dateText = div.selectFirst("span[q:key=Ee_0]")?.text()
				MangaChapter(
					id = generateUid(href),
					title = a.textOrNull(),
					number = i + 1f,
					volume = 0,
					url = href,
					uploadDate = parseChapterDate(
						dateFormat,
						dateText,
					),
					source = source,
					scanlator = null,
					branch = null,
				)
			},
		)
	}

	private fun parseChapterDate(dateFormat: DateFormat, date: String?): Long {
		val d = date?.lowercase() ?: return 0
		return when {
			WordSet(" ago").endsWith(d) -> {
				parseRelativeDate(d)
			}

			WordSet("just now").startsWith(d) -> {
				Calendar.getInstance().apply {
					set(Calendar.HOUR_OF_DAY, 0)
					set(Calendar.MINUTE, 0)
					set(Calendar.SECOND, 0)
					set(Calendar.MILLISECOND, 0)
				}.timeInMillis
			}

			else -> dateFormat.parseSafe(date)
		}
	}

	private fun parseRelativeDate(date: String): Long {
		val number = Regex("""(\d+)""").find(date)?.value?.toIntOrNull() ?: return 0
		val cal = Calendar.getInstance()
		return when {
			WordSet("second")
				.anyWordIn(date) -> cal.apply { add(Calendar.SECOND, -number) }.timeInMillis

			WordSet("minute", "minutes", "mins", "min")
				.anyWordIn(date) -> cal.apply { add(Calendar.MINUTE, -number) }.timeInMillis

			WordSet("hour", "hours")
				.anyWordIn(date) -> cal.apply { add(Calendar.HOUR, -number) }.timeInMillis

			WordSet("day", "days")
				.anyWordIn(date) -> cal.apply { add(Calendar.DAY_OF_MONTH, -number) }.timeInMillis

			WordSet("month", "months")
				.anyWordIn(date) -> cal.apply { add(Calendar.MONTH, -number) }.timeInMillis

			WordSet("year")
				.anyWordIn(date) -> cal.apply { add(Calendar.YEAR, -number) }.timeInMillis

			else -> 0
		}
	}


	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val doc = webClient.httpGet(chapter.url.toAbsoluteUrl(domain)).parseHtml()
		val id = chapter.url.removeSuffix('/').substringAfterLast('/').substringBefore('-')
		val s = doc.selectFirstOrThrow("script:containsData($id)").data()

		val script = if (s.contains("\"comic-")) {
			s.substringAfterLast("\"comic-")
		} else {
			s.substringAfterLast("\"manga-")
		}

		return Regex("\"(https?:.+?)\"")
			.findAll(script)
			.mapIndexedNotNullTo(ArrayList()) { i, it ->
				var url = it.groupValues.getOrNull(1) ?: return@mapIndexedNotNullTo null
                if (url.indexOf("//s") != -1 && url.indexOf(".") != -1){
                    var p = url.split("//")[1]
                    p = p.substring(p.indexOf("/"))
                    url = "https://$domain$p"
                }
				if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".jfif") || url.contains(".pjpeg") ||
					url.contains(".pjp") || url.contains(".png") || url.contains(".webp") || url.contains(".avif") ||
					url.contains(".gif")
				) {
					MangaPage(
						id = generateUid(url),
						url = url,
						preview = null,
						source = source,
					)
				} else {
					return@mapIndexedNotNullTo null
				}
			}
	}
}
