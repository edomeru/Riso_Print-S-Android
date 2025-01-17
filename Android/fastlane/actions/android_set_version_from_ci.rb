module Fastlane
  module Actions
    module SharedValues
      ANDROID_SET_VERSION_FROM_CI_CUSTOM_VALUE = :ANDROID_SET_VERSION_FROM_CI_CUSTOM_VALUE
    end

    class AndroidSetVersionFromCiAction < Action
      def self.run(params)
        # fastlane will take care of reading in the parameter and fetching the environment variable:
        branch_name = ENV["CI_COMMIT_REF_NAME"]
        puts branch_name
        x, version, build, date, *_ =  branch_name.split("/")
        date, *_ =   date.split("_")
        version = version[1..]
        version_name = "v"+version+"."+build
        version_code = version.gsub(".","0")+"00"+build

        puts "versionCode: #{version_code}"
        puts "versionName: #{version_name}"
        
        sh "sed -i.1 's/versionCode [0-9]*/versionCode #{version_code}/g'  app/build.gradle"
        sh "sed -i.1 's/versionCode [0-9]*/versionCode #{version_code}/g'  app/build.gradle"
        sh "mv app/build.gradle.1 app/build.gradle"

        sh "sed -i.1 's/versionName \"v[0-9.]*\"/versionName \"#{version_name}\"/g'  app/build.gradle"
        sh "sed -i.1 's/versionName \"v[0-9.]*\"/versionName \"#{version_name}\"/g'  app/build.gradle"
        sh "mv app/build.gradle.1 app/build.gradle"

        nil
        # Actions.lane_context[SharedValues::ANDROID_SET_VERSION_FROM_CI_CUSTOM_VALUE] = "my_val"
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "A short description with <= 80 characters of what this action does"
      end

      def self.details
        # Optional:
        # this is your chance to provide a more detailed description of this action
        "You can use this action to do cool things..."
      end

      def self.available_options
        # Define all options your action supports. 
        
        # Below a few examples
        [
          FastlaneCore::ConfigItem.new(key: :api_token,
                                       env_name: "FL_ANDROID_SET_VERSION_FROM_CI_API_TOKEN", # The name of the environment variable
                                       description: "API Token for AndroidSetVersionFromCiAction", # a short description of this parameter
                                       verify_block: proc do |value|
                                          UI.user_error!("No API token for AndroidSetVersionFromCiAction given, pass using `api_token: 'token'`") unless (value and not value.empty?)
                                          # UI.user_error!("Couldn't find file at path '#{value}'") unless File.exist?(value)
                                       end),
          FastlaneCore::ConfigItem.new(key: :development,
                                       env_name: "FL_ANDROID_SET_VERSION_FROM_CI_DEVELOPMENT",
                                       description: "Create a development certificate instead of a distribution one",
                                       is_string: false, # true: verifies the input is a string, false: every kind of value
                                       default_value: false) # the default value if the user didn't provide one
        ]
      end

      def self.output
        # Define the shared values you are going to provide
        # Example
        [
          ['ANDROID_SET_VERSION_FROM_CI_CUSTOM_VALUE', 'A description of what this value contains']
        ]
      end

      def self.return_value
        # If your method provides a return value, you can describe here what it does
      end

      def self.authors
        # So no one will ever forget your contribution to fastlane :) You are awesome btw!
        ["Your GitHub/Twitter Name"]
      end

      def self.is_supported?(platform)
        # you can do things like
        # 
        #  true
        # 
        #  platform == :ios
        # 
        #  [:ios, :mac].include?(platform)
        # 

        platform == :ios
      end
    end
  end
end
