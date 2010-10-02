package org.xmpp.packet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * When composing JIDs, two delimiter characters can be used:
 * <ul>
 * <li><tt>@</tt> (at)</li>
 * <li><tt>/</tt> (forward slash)</li>
 * </ul>
 * 
 * Tests in this class verify that usage of those characters as part of the JID is processed correctly.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class JIDDelimiterCharsTest {

	/**
	 * Case as reported in TINDER-47
	 */
	@Test
	public void testTINDER47() {
		final JID jid = new JID("a/@b");
		assertNull(jid.getNode());
		assertEquals("a", jid.getDomain());
		assertEquals("@b", jid.getResource());
	}

	/**
	 * Slashes are not allowed in the 'node' part of a JID. Beware that in some cases, "slashes in nodes" are actually
	 * the delimiter separating the domain and resource portions of a JID!
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_A() {
		new JID("/node@domain.com/resource");
	}

	@Test
	public void testSlashInNode_B() {
		final JID jid = new JID("n/ode@domain.com/resource");
		assertNull(jid.getNode());
		assertEquals("n", jid.getDomain());
		assertEquals("ode@domain.com/resource", jid.getResource());
	}

	@Test
	public void testSlashInNode_C() {
		final JID jid = new JID("node/@domain.com/resource");
		assertNull(jid.getNode());
		assertEquals("node", jid.getDomain());
		assertEquals("@domain.com/resource", jid.getResource());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_D() {
		new JID("//node@domain.com/resource");
	}

	@Test
	public void testSlashInNode_E() {
		final JID jid = new JID("node//@domain.com/resource");
		assertNull(jid.getNode());
		assertEquals("node", jid.getDomain());
		assertEquals("/@domain.com/resource", jid.getResource());
	}

	@Test
	public void testSlashInNode_F() {
		final JID jid = new JID("no//de@domain.com/resource");
		assertNull(jid.getNode());
		assertEquals("no", jid.getDomain());
		assertEquals("/de@domain.com/resource", jid.getResource());
	}

	@Test
	public void testSlashInNode_G() {
		final JID jid = new JID("n/o/de@domain.com/resource");
		assertNull(jid.getNode());
		assertEquals("n", jid.getDomain());
		assertEquals("o/de@domain.com/resource", jid.getResource());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_H() {
		new JID("/node", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_I() {
		new JID("n/ode", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_J() {
		new JID("node/", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_K() {
		new JID("//node", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_L() {
		new JID("node//", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_M() {
		new JID("no//de", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_N() {
		new JID("n/o/de", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_O() {
		new JID("/@domain.com/resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSlashInNode_P() {
		new JID("//@domain.com/resource");
	}

	/**
	 * At-characters are not allowed in the 'node' part of a JID (nor in domains).
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_A() {
		new JID("@node@domain.com/resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_B() {
		new JID("no@de@domain.com/resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_C() {
		new JID("node@@domain.com/resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_D() {
		new JID("@node", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_E() {
		new JID("n@ode", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_F() {
		new JID("node@", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_G() {
		new JID("@@node", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_H() {
		new JID("node@@", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_I() {
		new JID("no@@de", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_J() {
		new JID("n@o@de", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_K() {
		new JID("@", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_L() {
		new JID("@@", "domain.com", "resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_M() {
		new JID("@domain.com/resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_N() {
		new JID("@@domain.com/resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAtCharInNode_O() {
		new JID("@@@domain.com/resource");
	}

	/**
	 * Slashes <em>are</em> allowed in the 'resource' part of a JID.
	 */
	@Test
	public void testSlashInResource_A() {
		final JID jid = new JID("node@domain.com//resource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("/resource", jid.getResource());
	}

	@Test
	public void testSlashInResource_B() {
		final JID jid = new JID("node@domain.com/res/ource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res/ource", jid.getResource());
	}

	@Test
	public void testSlashInResource_C() {
		final JID jid = new JID("node@domain.com/resource/");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource/", jid.getResource());
	}

	@Test
	public void testSlashInResource_D() {
		final JID jid = new JID("node@domain.com///resource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("//resource", jid.getResource());
	}

	@Test
	public void testSlashInResource_E() {
		final JID jid = new JID("node@domain.com/resource//");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource//", jid.getResource());
	}

	@Test
	public void testSlashInResource_F() {
		final JID jid = new JID("node@domain.com/reso//urce");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("reso//urce", jid.getResource());
	}

	@Test
	public void testSlashInResource_G() {
		final JID jid = new JID("node@domain.com/res/our/ce");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res/our/ce", jid.getResource());
	}

	@Test
	public void testSlashInResource_H() {
		final JID jid = new JID("node@domain.com//");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("/", jid.getResource());
	}

	@Test
	public void testSlashInResource_I() {
		final JID jid = new JID("node@domain.com///");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("//", jid.getResource());
	}

	@Test
	public void testSlashInResource_J() {
		final JID jid = new JID("node@domain.com////");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("///", jid.getResource());
	}

	@Test
	public void testSlashInResource_K() {
		final JID jid = new JID("node", "domain.com", "/resource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("/resource", jid.getResource());
	}

	@Test
	public void testSlashInResource_L() {
		final JID jid = new JID("node", "domain.com", "reso/urce");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("reso/urce", jid.getResource());
	}

	@Test
	public void testSlashInResource_M() {
		final JID jid = new JID("node", "domain.com", "resource/");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource/", jid.getResource());
	}

	@Test
	public void testSlashInResource_N() {
		final JID jid = new JID("node", "domain.com", "//resource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("//resource", jid.getResource());
	}

	@Test
	public void testSlashInResource_O() {
		final JID jid = new JID("node", "domain.com", "reso//urce");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("reso//urce", jid.getResource());
	}

	@Test
	public void testSlashInResource_P() {
		final JID jid = new JID("node", "domain.com", "resource//");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource//", jid.getResource());
	}

	@Test
	public void testSlashInResource_Q() {
		final JID jid = new JID("node", "domain.com", "res/ourc/e");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res/ourc/e", jid.getResource());
	}

	@Test
	public void testSlashInResource_R() {
		final JID jid = new JID("node", "domain.com", "/");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("/", jid.getResource());
	}

	@Test
	public void testSlashInResource_S() {
		final JID jid = new JID("node", "domain.com", "//");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("//", jid.getResource());
	}

	@Test
	public void testSlashInResource_T() {
		final JID jid = new JID("node", "domain.com", "///");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("///", jid.getResource());
	}

	/**
	 * At-characters <em>are</em> allowed in the 'resource' part of a JID.
	 */
	@Test
	public void testAtCharInResource_A() {
		final JID jid = new JID("node@domain.com/@resource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("@resource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_B() {
		final JID jid = new JID("node@domain.com/res@ource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res@ource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_C() {
		final JID jid = new JID("node@domain.com/resource@");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource@", jid.getResource());
	}

	@Test
	public void testAtCharInResource_D() {
		final JID jid = new JID("node@domain.com/@@resource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("@@resource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_E() {
		final JID jid = new JID("node@domain.com/reso@@urce");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("reso@@urce", jid.getResource());
	}

	@Test
	public void testAtCharInResource_F() {
		final JID jid = new JID("node@domain.com/resource@@");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource@@", jid.getResource());
	}

	@Test
	public void testAtCharInResource_G() {
		final JID jid = new JID("node@domain.com/res@ou@rce");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res@ou@rce", jid.getResource());
	}

	@Test
	public void testAtCharInResource_H() {
		final JID jid = new JID("domain.com/@resource");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("@resource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_I() {
		final JID jid = new JID("domain.com/res@ource");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res@ource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_J() {
		final JID jid = new JID("domain.com/resource@");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource@", jid.getResource());
	}

	@Test
	public void testAtCharInResource_K() {
		final JID jid = new JID("domain.com/@@resource");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("@@resource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_L() {
		final JID jid = new JID("domain.com/reso@@urce");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("reso@@urce", jid.getResource());
	}

	@Test
	public void testAtCharInResource_M() {
		final JID jid = new JID("domain.com/resource@@");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource@@", jid.getResource());
	}

	@Test
	public void testAtCharInResource_N() {
		final JID jid = new JID("domain.com/res@ou@rce");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res@ou@rce", jid.getResource());
	}

	@Test
	public void testAtCharInResource_O() {
		final JID jid = new JID("node", "domain.com", "@resource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("@resource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_P() {
		final JID jid = new JID("node", "domain.com", "res@ource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res@ource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_Q() {
		final JID jid = new JID("node", "domain.com", "resource@");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource@", jid.getResource());
	}

	@Test
	public void testAtCharInResource_R() {
		final JID jid = new JID("node", "domain.com", "@@resource");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("@@resource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_S() {
		final JID jid = new JID("node", "domain.com", "reso@@urce");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("reso@@urce", jid.getResource());
	}

	@Test
	public void testAtCharInResource_T() {
		final JID jid = new JID("node", "domain.com", "resource@@");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource@@", jid.getResource());
	}

	@Test
	public void testAtCharInResource_U() {
		final JID jid = new JID("node", "domain.com", "res@ou@rce");
		assertEquals("node", jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res@ou@rce", jid.getResource());
	}

	@Test
	public void testAtCharInResource_V() {
		final JID jid = new JID(null, "domain.com", "@resource");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("@resource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_W() {
		final JID jid = new JID(null, "domain.com", "res@ource");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res@ource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_X() {
		final JID jid = new JID(null, "domain.com", "resource@");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource@", jid.getResource());
	}

	@Test
	public void testAtCharInResource_Y() {
		final JID jid = new JID(null, "domain.com", "@@resource");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("@@resource", jid.getResource());
	}

	@Test
	public void testAtCharInResource_Z() {
		final JID jid = new JID(null, "domain.com", "reso@@urce");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("reso@@urce", jid.getResource());
	}

	@Test
	public void testAtCharInResource_AA() {
		final JID jid = new JID(null, "domain.com", "resource@@");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("resource@@", jid.getResource());
	}

	@Test
	public void testAtCharInResource_AB() {
		final JID jid = new JID(null, "domain.com", "res@ou@rce");
		assertNull(jid.getNode());
		assertEquals("domain.com", jid.getDomain());
		assertEquals("res@ou@rce", jid.getResource());
	}

	/**
	 * Empty node and resource parts are forbidden.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testEmptyNode_A() {
		new JID("@domain.com/resource");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyNode_B() {
		new JID("@domain.com");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyResource_A() {
		new JID("node@domain.com/");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyResource_B() {
		new JID("domain.com/");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoDomain() {
		new JID("/test");
	}
}
