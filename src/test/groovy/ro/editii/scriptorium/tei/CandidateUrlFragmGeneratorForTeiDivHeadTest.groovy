package ro.editii.scriptorium.tei

import org.junit.jupiter.api.Test

import static ro.editii.scriptorium.GTestUtil.iterable2list

class CandidateUrlFragmGeneratorForTeiDivHeadTest {

    @Test
    void test_compute_unique_head_url_fragment_Protestație() {
        def iterable = new CandidateUrlFragmGeneratorForTeiDivHead(
                "Protestație în numele Moldovei, al omenirii și al lui Dumnezeu (1848)")

        def first_n = iterable2list(iterable, 9)

        assert ["protestatie",
                "protestatie_in_numele",
                "protestatie_in_numele_moldovei",
                "protestatie_in_numele_moldovei_al_omenirii",
                "protestatie_in_numele_moldovei_al_omenirii_si_al_lui_dumnezeu",
                "protestatie_in_numele_moldovei_al_omenirii_si_al_lui_dumnezeu_1848",
                "protestatie_in_numele_moldovei_al_omenirii_si_al_lui_dumnezeu_1848_2",
                "protestatie_in_numele_moldovei_al_omenirii_si_al_lui_dumnezeu_1848_3",
                "protestatie_in_numele_moldovei_al_omenirii_si_al_lui_dumnezeu_1848_4"] == first_n


    }

    @Test
    void test_compute_unique_head_url_fragment_16martie() {

        final iterable = new CandidateUrlFragmGeneratorForTeiDivHead("16 martie 2015")

        final first_n = iterable2list(iterable, 4)
        println first_n

        assert ["16_martie_2015", "16_martie", "16_martie_2015_2", "16_martie_2015_3"] == first_n

    }

    @Test
    void test_1dot() {
        final onedot = '1.'
        final oneromandot = 'I.'

        def list = iterable2list(new CandidateUrlFragmGeneratorForTeiDivHead(onedot))
        println list
        assert list.first() == '1'

        list = iterable2list(new CandidateUrlFragmGeneratorForTeiDivHead(oneromandot))
        println(list)
        assert list.first() == 'i'
    }

    @Test
    void test_empty_head() {

        final empty1 = ''
        final empty2 = ' '
        final empty3 = '  \t'

        def list = iterable2list(new CandidateUrlFragmGeneratorForTeiDivHead(empty1))
        println list
        assert list[0..3] == ['1', '2', '3', '4']

        list = iterable2list(new CandidateUrlFragmGeneratorForTeiDivHead(empty2))
        println(list)
        assert list.first() == '1'
        assert list[1] == '2'

        list = iterable2list(new CandidateUrlFragmGeneratorForTeiDivHead(empty3))
        println(list)
        assert list[0..1] == ['1', '2']

        try {
            new CandidateUrlFragmGeneratorForTeiDivHead(null)
        } catch (AssertionError e) {
            // good
        }
    }

    @Test
    void testPonctuation() {
        final head = "À L’AVENTURE !"
        final gen = new CandidateUrlFragmGeneratorForTeiDivHead(head)
        final candidates = iterable2list(gen)
        p candidates
        assert candidates[0..3] == ['a_laventure', 'a_laventure_2', 'a_laventure_3', 'a_laventure_4'];
    }

    @Test
    void notre_monde_flatland() {
        final string = "NOTRE MONDE – FLATLAND"
        expect(string, "notre_monde_flatland")
    }

    @Test
    void histoireDelaRevolutionFrancaise() {
            expect("Histoire de la Révolution française",
                    "histoire_de_la_revolution_francaise")
    }

    @Test
    void testParsign() {
        expect("§ foaie", "foaie")
    }

    @Test
    void cyrillic() {
        final head1 = "Достоевский и его наследие"
        final head2 = "Бедные люди"
        def candidates = iterable2list(new CandidateUrlFragmGeneratorForTeiDivHead(head1))
        assert candidates.first() == 'dostoyevskii_i_yego_naslediye'
        candidates = iterable2list(new CandidateUrlFragmGeneratorForTeiDivHead(head2))
        assert candidates.first() == 'bednyye_lyudi'
    }

    @Test
    void spacesTrimmed() {
        def head = "   Volume"
        assert head.startsWith("  ")
        def candidates = iterable2list(new CandidateUrlFragmGeneratorForTeiDivHead(head))
        assert candidates.first() == 'volume'

        head = "         Capitolul X."
        candidates = iterable2list(new CandidateUrlFragmGeneratorForTeiDivHead(head))
        p candidates
        assert candidates.first() == 'capitolul_x'
    }

    def expect(String source, String result) {
        final gen = new CandidateUrlFragmGeneratorForTeiDivHead(source)
        final res = gen.iterator().next()
        assert res == result
    }

    def p(args) { println args }
}