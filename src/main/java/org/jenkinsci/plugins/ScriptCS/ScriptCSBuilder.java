package org.jenkinsci.plugins.ScriptCS;

import hudson.Launcher;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ArgumentListBuilder;
import hudson.util.VariableResolver;
import java.io.File;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link ScriptCSBuilder} is created. The created instance is persisted to the
 * project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #name}) to remember the configuration.
 *
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
 * invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class ScriptCSBuilder extends Builder {

    private final String scriptfile;
    private final String arguments;
    private final String customScript;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ScriptCSBuilder(String scriptfile, String arguments, String customScript) {
        this.scriptfile = scriptfile;
        this.arguments = arguments;
        this.customScript = customScript;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     *
     * @return
     */
    public String getscriptfile() {
        return scriptfile;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     *
     * @return
     */
    public String getarguments() {
        return arguments;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     *
     * @return
     */
    public String getcustomScript() {
        return customScript;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {

        ArgumentListBuilder args = new ArgumentListBuilder();

        String execName = getDescriptor().scriptcsexe;

        args.add(execName);

        File customScriptFile = null;

        if (StringUtils.isNotEmpty(customScript)) {
            listener.getLogger().println("Using custom script");

            customScriptFile = File.createTempFile("ScriptCS_", ".csx");

            PrintWriter out = new PrintWriter(customScriptFile);

            out.print(customScript);

            out.flush();

            out.close();

            args.add(customScriptFile.toPath().toString());
        } else {
            args.add(scriptfile);
        }

        if (StringUtils.isNotEmpty(arguments)) {
            args.add("--");
            args.add(arguments);
        }

        //Try to execute the command
        listener.getLogger().println("Executing command: " + args.toString());
        Map<String, String> env = build.getEnvironment(listener);
        try {
            int r = launcher.launch().cmds(args).envs(env).stdout(listener).pwd(build.getModuleRoot()).join();
            return r == 0;
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("command execution failed"));
            return false;
        } finally {
            try {
                if (customScriptFile != null) {
                    customScriptFile.delete();
                }
            } catch (Exception ex2) {
            }
        }
    }

    // Overridden for better type safety.
// If your plugin doesn't really define any property on Descriptor,
// you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();

    }

    /**
     * Descriptor for {@link ScriptCSBuilder}. Used as a singleton. The class is
     * marked as public so that it can be accessed from views.
     *
     * <p>
     * See
     * <tt>src/main/resources/hudson/plugins/hello_world/ScriptCSBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * To persist global configuration information, simply store it in a
         * field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String scriptcsexe;

        /**
         * In order to load the persisted global configuration, you have to call
         * load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set a name");
            }
            if (value.length() < 4) {
                return FormValidation.warning("Isn't the name too short?");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "ScriptCS Runner";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            scriptcsexe = formData.getString("scriptcsexe");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req, formData);
        }

        /**
         * This method returns true if the global configuration says we should
         * speak French.
         *
         * The method name is bit awkward because global.jelly calls this method
         * to determine the initial state of the checkbox by the naming
         * convention.
         */
        public String getScriptExe() {
            return scriptcsexe;
        }
    }
}
