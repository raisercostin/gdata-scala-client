/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.google.gdata.data;

import com.google.xml.combinators.{Picklers, ~}
import com.google.gdata.data.util.DateTime

import Picklers._
import Atom._

/**
 * A module for Feeds. Use this by mixing it in a class that has an Entries implementation.
 *
 * This module provides a feed and a feed pickler.
 * This module requires an entry type and an entry pickler.
 *
 * @author Iulian Dragos
 */
trait Feeds { this: Feeds with Entries =>
  type Feed
  
  /** A pickler for feeds. */
  def feedPickler: Pickler[Feed] = elem("feed", feedContentsPickler)(Uris.atomNs)
  
  /** An abstract pickler for feed contents. Subclasses need to implement this method. */
  def feedContentsPickler: Pickler[Feed]
}

/**
 * Atom feeds refines Feeds with Atom-like feeds.
 */
trait AtomFeeds extends Feeds { this: AtomFeeds with Entries =>
  type Feed <: AtomFeed

  /** An Atom Feed. */
  class AtomFeed extends AnyRef {
    /** The authors of this feed. */
    var authors: List[Person] = Nil
    
    /** Categories associated to this feed. */
    var categories: List[Category] = Nil
    
    /** Contributors associated to this feed. */
    var contributors: List[Person] = Nil
    
    /** User agent used to generate this feed. */
    var generator: Option[Generator] = None
    
    /** IRI for an icon for this feed. */
    var icon: Option[String] = None
    
    /** Permanent, unique id for this feed. */
    var id: String = ""
    
    /** References to web resources. */
    var links: List[Link] = Nil
    
    /** IRI for a visual identification of this feed. */
    var logo: Option[String] = None
    
    /** Rights held over this feed. */
    var rights: Option[String] = None
    
    /** A subtitle of this feed. */
    var subtitle: Option[Text] = None
    
    /** This feed's title. */
    var title: Text = NoText
    
    /** The most recent instant in time this feed has been modified. */
    var updated: DateTime = new DateTime(new java.util.Date())
    
    /** A list of entries contained in this feed. */
    var entries: List[Entry] = Nil
    
    /** The number of search results available for this query. */
    var totalResults: Option[Int] = None
    
    /** The index of the first search result in the current set of search results. */
    var startIndex: Option[Int] = None
    
    /** The number of search results returned per page. */
    var itemsPerPage: Option[Int] = None
    
    /** Initialization method to fill all known fields. */
    def fillOwnFields(authors: List[Person], categories: List[Category], contributors: List[Person],
        generator: Option[Generator], icon: Option[String], id: String, links: List[Link],
        logo: Option[String], rights: Option[String], subtitle: Option[Text], title: Text,
        updated: DateTime, totalResults: Option[Int], startIndex: Option[Int],
        itemsPerPage: Option[Int], entries: List[Entry]): this.type = {
      this.authors = authors
      this.categories = categories
      this.contributors = contributors
      this.generator = generator
      this.icon = icon
      this.id = id
      this.links = links
      this.logo = logo
      this.rights = rights
      this.subtitle = subtitle
      this.title = title
      this.updated = updated
      this.totalResults = totalResults
      this.startIndex = startIndex
      this.itemsPerPage = itemsPerPage
      this.entries = entries
      this
    }

    /** Copy known fields from another AtomFeed. */
    def fromAtomFeed(af: AtomFeed) {
      fillOwnFields(af.authors, af.categories, af.contributors, af.generator, af.icon, af.id,
          af.links, af.logo, af.rights, af.subtitle, af.title, af.updated, af.totalResults,
          af.startIndex, af.itemsPerPage, af.entries)
    }
    
    override def toString = {
      val sb = new StringBuffer(256) // override the ridiculous 16-chars default size
      sb.append("Authors: ").append(authors.mkString("", ", ", ""))
        .append("\nCategories: ").append(categories.mkString("", ", ", ""))
        .append("\nContributors: ").append(contributors.mkString("", ", ", ""))
        .append("\nGenerator: ").append(generator)
        .append("\nIcon: ").append(icon)
        .append("\nId: ").append(id)
        .append("\nLinks: ").append(links.mkString("", ", ", ""))
        .append("\nLogo: ").append(logo)
        .append("\nRights: ").append(rights)
        .append("\nSubtitle: ").append(subtitle)
        .append("\nTitle: ").append(title)
        .append("\nUpdated: ").append(updated)
        .append("\nTotal Results: ").append(totalResults)
        .append("\nStart index: ").append(startIndex)
        .append("\nItems per page: ").append(itemsPerPage)
        .append("\nEntries: ").append(entries.mkString("", "\n\t", ""))
        .toString
    }
  }

  lazy val atomFeedContents = {
    implicit val ns = Uris.atomNs
    
    interleaved(
        rep(atomPerson("author"))
      ~ rep(Category.pickler)
      ~ rep(atomPerson("contributor"))
      ~ opt(Generator.pickler)
      ~ opt(elem("icon", text))
      ~ elem("id", text)
      ~ rep(Link.pickler)
      ~ opt(elem("logo", text))
      ~ opt(elem("rights", text))
      ~ opt(atomText("subtitle"))
      ~ atomText("title")
      ~ elem("updated", dateTime)
      ~ opt(elem("totalResults", intVal)(Uris.openSearchNs))
      ~ opt(elem("startIndex", intVal)(Uris.openSearchNs))
      ~ opt(elem("itemsPerPage", intVal)(Uris.openSearchNs))
      ~ rep(entryPickler))
  }

  lazy val atomFeedContentsPickler: Pickler[AtomFeed] = wrap (atomFeedContents) ({
    case authors ~ cats ~ contribs ~ generator ~ icon ~ id
         ~ links ~ logo ~ rights ~ subtitle ~ title ~ updated ~ totalResults
         ~ startIndex ~ itemsPerPage ~ entries => 
      (new AtomFeed).fillOwnFields(authors, cats, contribs, generator, icon, id,
          links, logo, rights, subtitle, title, updated, totalResults, startIndex, 
          itemsPerPage, entries)
  }) (fromFeed)

  private def fromFeed(e: AtomFeed) = (new ~(e.authors, e.categories) 
      ~ e.contributors ~ e.generator ~ e.icon ~ e.id ~ e.links ~ e.logo ~ e.rights
      ~ e.subtitle ~ e.title ~ e.updated ~ e.totalResults ~ e.startIndex 
      ~ e.itemsPerPage ~ e.entries)
}
