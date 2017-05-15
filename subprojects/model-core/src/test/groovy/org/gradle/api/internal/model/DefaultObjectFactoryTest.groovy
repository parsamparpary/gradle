/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.model

import org.gradle.api.Named
import org.gradle.test.fixtures.concurrent.ConcurrentSpec
import org.gradle.util.Matchers
import spock.lang.Ignore

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CyclicBarrier

class DefaultObjectFactoryTest extends ConcurrentSpec {
    def factory = DefaultObjectFactory.INSTANCE

    def "creates instance of Named"() {
        expect:
        def n1 = factory.named(Named, "a")
        def n2 = factory.named(Named, "b")

        n1.is(n1)
        !n1.is(n2)

        n1.name == "a"
        n2.name == "b"

        Matchers.strictlyEquals(n1, n1)
        n2 != n1
        !n2.equals(n1)

        n1.hashCode() == n1.hashCode()
        n1.hashCode() != n2.hashCode()

        n1.toString() == "a"
        n2.toString() == "b"

        n1.is(factory.named(Named, "a"))
        n2.is(factory.named(Named, "b"))
    }

    def "creates instance of subtype of Named"() {
        expect:
        def n1 = factory.named(CustomNamed, "a")
        def n2 = factory.named(CustomNamed, "b")

        n1.is(n1)
        !n1.is(n2)

        n1.name == "a"
        n2.name == "b"

        Matchers.strictlyEquals(n1, n1)
        n2 != n1
        !n2.equals(n1)

        n1.hashCode() == n1.hashCode()
        n1.hashCode() != n2.hashCode()

        n1.toString() == "a"
        n2.toString() == "b"

        n1.is(factory.named(CustomNamed, "a"))
        n2.is(factory.named(CustomNamed, "b"))

        !n1.is(factory.named(Named, "a"))
        !n2.is(factory.named(Named, "b"))
    }

    def "multiple threads can create different instances of same type"() {
        def results = new CopyOnWriteArrayList()
        def barrier = new CyclicBarrier(10)

        when:
        async {
            10.times { n ->
                start {
                    barrier.await()
                    results.add(factory.named(CustomNamed, n as String))
                }
            }
        }

        then:
        results.size() == 10
        results.unique().size() == 10
    }

    def "multiple threads can create same instance"() {
        def results = new CopyOnWriteArrayList()
        def barrier = new CyclicBarrier(10)

        when:
        async {
            10.times { n ->
                start {
                    barrier.await()
                    results.add(factory.named(CustomNamed, "value"))
                }
            }
        }

        then:
        results.size() == 10
        results.unique().size() == 1
    }

    @Ignore
    def "creates instance of abstract class"() {
        expect: false
    }

    @Ignore
    def "interface may not have additional methods"() {
        expect: false
    }

    @Ignore
    def "abstract class may not have additional abstract methods"() {
        expect: false
    }
}